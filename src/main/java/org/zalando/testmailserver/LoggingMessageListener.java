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
