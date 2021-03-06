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
