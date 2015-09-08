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
package org.zalando.testmailserver;

import static org.zalando.testmailserver.util.FormattingUtils.format;

import java.lang.management.ManagementFactory;
import java.net.BindException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;
import org.zalando.testmailserver.util.Utils;

import ch.qos.logback.classic.LoggerContext;

public class TestMailServer implements AutoCloseable {

	private static final Logger LOG = LoggerFactory
			.getLogger(TestMailServer.class);

	public static enum Message {
		NAME, LISTENING, COULD_NOT_BIND, STOPPED
	}

	public static void main(final String[] args) throws Exception {
		try {
			System.out.println(format("{}={}",Message.NAME, ManagementFactory.getRuntimeMXBean().getName()));
			final Thread mainThread = Thread.currentThread();
			final AtomicBoolean shouldRun = new AtomicBoolean(false);
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				shutdown(mainThread, shouldRun);
			}, "shutdown-hook"));
			final TestMailServer testMailServer = new TestMailServer(
					getPort(args));
			try {
				try {
					testMailServer.start();
					shouldRun.set(true);
					System.out.println(Message.LISTENING);
				} catch (final BindException be) {
					System.out.println(Message.COULD_NOT_BIND);
					throw be;
				}
				while (shouldRun.get()) {
					try {
						Thread.sleep(Long.MAX_VALUE);
						LOG.debug("Finished waiting.");
					} catch (final InterruptedException e) {
						LOG.debug("Received interrupt while waiting.");
					}
				}
			} finally {
				testMailServer.close();
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		} finally {
			System.out.println(Message.STOPPED);
		}
	}

	private static int getPort(final String[] args) {
		if (args == null ? true : args.length < 1)
			return 25;
		else
			return Integer.parseInt(args[0]);
	}

	private static void shutdown(final Thread mainThread,
			final AtomicBoolean shouldRun) {
		LOG.debug("Shutting down.");
		shouldRun.set(false);
		mainThread.interrupt();
		LOG.debug("Waiting until main thread terminates.");
		try {
			mainThread.join(0);
		} catch (final InterruptedException e) {
			LOG.error("Interrupted while waiting for main thread to terminate.");
		}
		LOG.debug("Main thread terminated.");
		final LoggerContext loggerContext = (LoggerContext) LoggerFactory
				.getILoggerFactory();
		LOG.info("Server shut down, shutting down logging now, too. Goodbye.");
		loggerContext.stop();
	}

	private final SMTPServer server;

	public TestMailServer(final int port) throws Exception {
		LOG.info("Creating testmailserver.");
		final SimpleMessageListener listener = new LoggingMessageListener();
		final MessageHandlerFactory handlerFactory = new SimpleMessageListenerAdapter(
				listener);
		server = new SMTPServer(handlerFactory);
		server.setHostName(Utils.getDefaultHostName());
		server.setPort(port);
	}

	public void start() throws BindException {
		try {
			server.start();
		} catch (final RuntimeException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof BindException)
				throw (BindException) cause;
			else
				throw e;
		}
	}

	@Override
	public void close() throws Exception {
		server.stop();
	}

}
