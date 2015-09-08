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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.zalando.testmailserver.util.Utils;

public class MailServerManualtest {

	@Test
	public void test() throws Exception {
		final int port = 2025;
		final Path logDir = FileSystems.getDefault().getPath("testlogs/msg").toAbsolutePath();
		int countBeforeMail;
		try (TestMailServer testMailServer = new TestMailServer(port)) {
			testMailServer.start();
			countBeforeMail = Utils.countDirectChildren(logDir);
			sendMail(port);
		}
		assertThat(Files.isDirectory(logDir), is(true));
		assertThat(Utils.countDirectChildren(logDir), is(countBeforeMail+1));
	}

	static void sendMail(final int port) throws MessagingException {
		final Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", "localhost");
		properties.setProperty("mail.smtp.port", Integer.toString(port));
		final String from = "sender@test";
		final String to = "recipient@test";
		final Session session = Session.getDefaultInstance(properties);
		final MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject("Subject 1");
		message.setText("This is the message.");
		Transport.send(message);
	}

}
