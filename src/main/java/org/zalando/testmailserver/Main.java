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

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;
import org.zalando.testmailserver.util.Utils;

import ch.qos.logback.classic.LoggerContext;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		LOG.debug("Started to execute main method.");
		final Thread mainThread = Thread.currentThread();
		final AtomicBoolean shouldRun = new AtomicBoolean(true);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			shutdown(mainThread, shouldRun);
		},"shutdown-hook"));
		
		final SimpleMessageListener listener = new LoggingMessageListener();
		final MessageHandlerFactory handlerFactory = new SimpleMessageListenerAdapter(listener);
		final SMTPServer server = new SMTPServer(handlerFactory);
		server.setHostName(Utils.getDefaultHostName());
		server.setPort(25);

		server.start();

		try{
			while (shouldRun.get()) {
				try {
					Thread.sleep(Long.MAX_VALUE);
					LOG.debug("Finished waiting.");
				} catch (InterruptedException e) {
					LOG.debug("Received interrupt while waiting.");
				}
			}
		}
		finally{;}
	}

	private static void shutdown(Thread mainThread, AtomicBoolean shouldRun) {
		LOG.info("Shutting down.");
		shouldRun.set(false);
		mainThread.interrupt();
		LOG.debug("Waiting until main thread terminates.");
		try {
			mainThread.join(0);
		} catch (InterruptedException e) {
			LOG.error("Interrupted while waiting for main thread to terminate.");
		}
		LOG.debug("Main thread terminated.");
		LoggerContext loggerContext = (LoggerContext) LoggerFactory
				.getILoggerFactory();
		LOG.info("Shutting down logging now, goodbye.");
		loggerContext.stop();
	}

}
