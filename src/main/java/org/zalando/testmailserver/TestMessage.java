package org.zalando.testmailserver;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TestMessage{
	byte[] messageData;
	String envelopeSender;
	String envelopeReceiver;

	TestMessage(final String envelopeSender, 
				final String envelopeReceiver, 
				final byte[] messageData){
		this.envelopeSender = envelopeSender;
		this.envelopeReceiver = envelopeReceiver;
		this.messageData = messageData;
	}

	/**
	 * Get's the raw message DATA.
	 */
	public byte[] getData()
	{
		return messageData.clone();
	}

	/**
	 * Get's the RCPT TO:
	 */
	public String getEnvelopeReceiver()
	{
		return envelopeReceiver;
	}

	/**
	 * Get's the MAIL FROM:
	 */
	public String getEnvelopeSender()
	{
		return envelopeSender;
	}

	@Override
	public String toString(){
		final StringBuilder result = new StringBuilder();
		result.append("Envelope sender: " + getEnvelopeSender()).append('\n');
		result.append("Envelope recipient: " + getEnvelopeReceiver()).append('\n');
		result.append('\n');
		result.append(new String(getData(), UTF_8));
		return result.toString();
	}
}
