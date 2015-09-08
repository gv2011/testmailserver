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
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.zalando.testmailserver.util.FormattingUtils.format;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.testmailserver.TestMailServer.Message;
import org.zalando.testmailserver.util.Utils;


public class MailServerIT {

	private static final Logger LOG = LoggerFactory
			.getLogger(MailServerIT.class);

	@Test
	public void test() throws Exception {
		final Runtime rt = Runtime.getRuntime();
		final FileSystem fs = FileSystems.getDefault();
		final Path logDir = fs.getPath("logs/msg").toAbsolutePath();
		assertTrue(Files.isDirectory(logDir) || !Files.exists(logDir));
		final Path jarFile = fs.getPath("target/testmailserver.jar");
		assertTrue(Files.isRegularFile(jarFile));
		assumeTrue(killIsAvailable(rt));
		int port = 1025;
		boolean bound = false;
		int count = 0;
		while (!bound && count < 10) {
			count++;
			final String cmd = "java -jar "+jarFile+" " + port;
			LOG.info("Starting testmailserver on port {} with command {}.", port, cmd);
			final Process p = rt.exec(cmd);
			int pid = -1;
			try {
				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(p.getInputStream()));
				final String nameMsg = reader.readLine();
				pid = getPid(nameMsg);
				LOG.info("Process id is {}.", pid);
				final Message m = Message.valueOf(reader.readLine());
				if (m == Message.COULD_NOT_BIND) {
					LOG.info(
							"Could not bind to port {}. Trying again with different port.",
							port);
					port++;
				} else {
					assertThat(m, is(Message.LISTENING));
					bound = true;
					
					final int msgCount = Utils.countDirectChildren(logDir);
					LOG.info("Currently there are log entries for {} messages.", msgCount);
					MailServerManualtest.sendMail(port);
					assertThat(Utils.countDirectChildren(logDir), is(msgCount+1));
					LOG.info("Now the number of message increased by one, as expected.");
					
					rt.exec("kill -INT " + pid);
					LOG.info("Sent SIGINT to terminate the testmailserver (pid {}).", pid);
				}
				LOG.debug("Waiting for process {} to terminate.", pid);
				final boolean terminated = p.waitFor(10, TimeUnit.SECONDS);
				assertTrue(format("Process {} did not terminate after 10 seconds.",pid), terminated);
			} finally {
				if (p.isAlive()){
					LOG.error("Process {} did not react. Terminating forcibly.", pid);
					p.destroyForcibly();
				}
				final int returnCode = p.waitFor();
				if(returnCode==0 || returnCode==130)
					LOG.info("Process {} terminated with exit code {}.", pid, returnCode);
				else
					LOG.warn("Process {} terminated with unexpected exit code {}.", pid, returnCode);
			}
		}
		assertTrue(format("Could not bind after {} tries.", count), bound);
	}

	private boolean killIsAvailable(final Runtime rt) {
		try {
			final Process p = rt.exec("kill -l");
			try{return p.waitFor()==0;}
			finally{if(p.isAlive()) p.destroyForcibly();}
		} catch (final Exception e) {
			LOG.warn("Could not execute kill -l", e);
			return false;
		}
	}

	private int getPid(final String nameMsg) {
		final String prefix = Message.NAME + "=";
		assertTrue(nameMsg.startsWith(prefix));
		final String pName = nameMsg.substring(prefix.length());
		return Integer.parseInt(pName.substring(0, pName.indexOf('@')));
	}

}
