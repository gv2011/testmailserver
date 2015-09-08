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

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.helper.SimpleMessageListener;

import com.google.common.io.ByteStreams;

/**
 * This listener accepts all messages and logs them. Only suitable for small messages, because
 * they are kept in memory and not streamed.
 */
public final class LoggingMessageListener implements SimpleMessageListener
{
	private final static Logger LOG = LoggerFactory.getLogger(LoggingMessageListener.class);
	
	public static final String MSG_LOG_NAME = LoggingMessageListener.class.getName()+".msg";
	private final static Logger MSG_LOG = LoggerFactory.getLogger(MSG_LOG_NAME);
	
	/** Always accept everything */
	@Override
	public boolean accept(final String from, final String recipient)
	{
		if (LOG.isDebugEnabled())
			LOG.debug("Accepting mail from " + from + " to " + recipient);

		return true;
	}

	@Override
	public void deliver(final String from, final String recipient, final InputStream data) 
			throws IOException
	{
		if (LOG.isDebugEnabled())
			LOG.debug("Logging mail from " + from + " to " + recipient);

		final byte[] bytes = ByteStreams.toByteArray(data);

		LOG.debug("Message data length is {}.", bytes.length);

		final TestMessage msg = new TestMessage(from, recipient, bytes);
		MSG_LOG.debug(msg.toString());
	}


}
