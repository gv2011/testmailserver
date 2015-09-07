/**
 * The MIT License
 * Copyright (c) 2015 Zalando SE
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.zalando.testmailserver.logbackext;

import static org.zalando.testmailserver.util.FormattingUtils.format;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import org.zalando.testmailserver.util.LazyVar;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;

public abstract class AbstractAppender<E> implements Appender<E> {

	private final FilterAttachableImpl<E> fai = new FilterAttachableImpl<E>();

	private final LazyVar<Context> context = new LazyVar<Context>("context");
	private final LazyVar<String> name = new LazyVar<String>("name");

	private final ReadLock appendLock;
	private final WriteLock closeLock;

	private static enum State {
		INITIAL, ACTIVE, STOPPING, STOPPED, ERROR
	}

	private final Object stateGuard = new Object();
	@GuardedBy("readLock,writeLock")
	private State state = State.INITIAL;

	protected AbstractAppender() {
		final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		appendLock = lock.readLock();
		closeLock = lock.writeLock();
	}

	@Override
	public String getName() {
		return name.get();
	}

	@Override
	public void setName(final String name) {
		this.name.set(name);
	}

	@Override
	public Context getContext() {
		return context.get();
	}

	@Override
	public void setContext(final Context context) {
		this.context.set(context);
	}

	public StatusManager getStatusManager() {
		final StatusManager statusManager = getContext().getStatusManager();
		if (statusManager == null)
			throw new IllegalStateException("No status manager.");
		return statusManager;
	}

	/**
	 * @return this as declared origin.
	 */
	protected final Object getDeclaredOrigin() {
		return this;
	}

	@Override
	public String toString() {
		return format("{}[{}]", getClass().getName(), name.getOptional());
	}

	@Override
	public void addStatus(final Status status) {
		getStatusManager().add(status);
	}

	@Override
	public void addInfo(final String msg) {
		addStatus(new InfoStatus(msg, getDeclaredOrigin()));
	}

	@Override
	public void addInfo(final String msg, final Throwable ex) {
		addStatus(new InfoStatus(msg, getDeclaredOrigin(), ex));
	}

	@Override
	public void addWarn(final String msg) {
		addStatus(new WarnStatus(msg, getDeclaredOrigin()));
	}

	@Override
	public void addWarn(final String msg, final Throwable ex) {
		addStatus(new WarnStatus(msg, getDeclaredOrigin(), ex));
	}

	@Override
	public void addError(final String msg) {
		addError(msg, null);
	}

	@Override
	public void addError(final String msg, final @Nullable Throwable ex) {
		boolean doAdd;
		synchronized (stateGuard) {
			doAdd = state != State.ERROR;
			state = State.ERROR;
		}
		if (doAdd) {
			final ErrorStatus status;
			if (ex == null)
				status = new ErrorStatus(msg, getDeclaredOrigin());
			else
				status = new ErrorStatus(msg, getDeclaredOrigin(), ex);
			addStatus(status);
		}
	}

	@Override
	public void addFilter(final Filter<E> newFilter) {
		fai.addFilter(newFilter);
	}

	@Override
	public void clearAllFilters() {
		fai.clearAllFilters();
	}

	@Override
	public List<Filter<E>> getCopyOfAttachedFiltersList() {
		return fai.getCopyOfAttachedFiltersList();
	}

	@Override
	public FilterReply getFilterChainDecision(final E event) {
		return fai.getFilterChainDecision(event);
	}

	@Override
	public void start() {
		synchronized (stateGuard) {
			if (state != State.INITIAL) {
				throw new IllegalStateException(state.toString());
			} else
				state = State.ACTIVE;
		}
	}

	@Override
	public void stop() {
		synchronized (stateGuard) {
			if (state != State.ACTIVE) {
				throw new IllegalStateException(state.toString());
			} else
				state = State.STOPPING;
		}
		closeLock.lock();
		try {
			if (state != State.STOPPING)
				throw new IllegalStateException(state.toString());
			state = State.STOPPED;
		} finally {
			closeLock.unlock();
		}
	}

	@Override
	public boolean isStarted() {
		synchronized (stateGuard) {
			return state == State.ACTIVE;
		}
	}

	@Override
	public void doAppend(final E eventObject) {
		appendLock.lock();
		try {
			synchronized (stateGuard) {
				if (state != State.ACTIVE)
					throw new IllegalStateException(state.toString());
			}
			final FilterReply decision = getFilterChainDecision(eventObject);
			if (decision != FilterReply.DENY) append(eventObject);
		} catch (final Exception e) {
			addError("Appender [" + name + "] failed to append.", e);
		} finally {
			appendLock.unlock();
		}
	}

	protected abstract void append(final E event) throws Exception;

}
