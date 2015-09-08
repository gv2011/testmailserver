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
package org.zalando.testmailserver.util;


import static org.zalando.testmailserver.util.FormattingUtils.format;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

	private final static Logger LOG = LoggerFactory.getLogger(Utils.class);
	
	public static String getDefaultHostName(){
		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getCanonicalHostName();
		}
		catch (final UnknownHostException e){
			final String defaultHostName = "localhost";
			LOG.warn(format("Could not obtain hostname. Using {}.", defaultHostName), e);
			hostName = defaultHostName;
		}
		return hostName;
	}
	
	public static int countDirectChildren(final Path directory) throws IOException {
		final AtomicInteger count = new AtomicInteger();
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
			dirStream.forEach((final Path p) -> {
				count.incrementAndGet();
			});
		}
		return count.get();
	}



}
