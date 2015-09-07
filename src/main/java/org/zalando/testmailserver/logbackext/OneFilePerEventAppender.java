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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zalando.testmailserver.util.LazyVar;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

/**
 * Creates a new file with a timestamp file name for each log message.
 * 
 * Purpose: Useful for low-frequency, important log messages. Allows to
 * get get some information by just looking at the log folder.
 */
public class OneFilePerEventAppender<E> extends AbstractAppender<E> {
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH.mm.ss.SSSZ'.log'");

	private final LazyVar<Path> folder = new LazyVar<>("folder");
	private final LazyVar<Layout<E>> layout = new LazyVar<>("layout");


	public Layout<E> getLayout() {
		return layout.get();
	}

	public void setLayout(final Layout<E> layout) {
		this.layout.set(layout);
	}

	public String getFolder() {
		return folder.get().toString();
	}
	
	public void setFolder(final String folder) throws IOException {
		final Path f = FileSystems.getDefault().getPath(folder).toAbsolutePath();
		if (!Files.isDirectory(f)) {
			if (Files.exists(f))
				throw new IllegalArgumentException(f + " is not a directory.");
			Files.createDirectories(f);
		}
		this.folder.set(f);
	}

	@Override
	protected void append(final E event) throws IOException, InterruptedException {
		OutputStream outputStream = null;
		while (outputStream == null) {
			final Date now = new Date();
			final Path file = folder.get().resolve(DATE_FORMAT.format(now));
			try {outputStream = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW);}
			catch (final FileAlreadyExistsException ex) {
				//File names contain system time in milliseconds, so if
				//the file name already exists, we can try again a millisecond
				//later.
				Thread.sleep(1);
			}
		}
		try {
			final Encoder<E> encoder = createEncoder(outputStream);
			try {
				encoder.doEncode(event);
			} finally {
				encoder.close();
			}
		} finally {
			outputStream.close();
		}
	}
	
	private Encoder<E> createEncoder(final OutputStream outputStream)
			throws IOException {
		final LayoutWrappingEncoder<E> lwe = new LayoutWrappingEncoder<E>();
		lwe.setLayout(layout.get());
		lwe.setContext(getContext());
		lwe.init(outputStream);
		return lwe;
	}



}
