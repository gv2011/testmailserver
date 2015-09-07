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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * A variable/reference that can be set once to a non-null value. A read access will
 * return that value or throw an IllegalStateException if it has not been set yet.
 * 
 * Purpose: Allow initialization by setters after construction, but provide read-only
 * semantics and thread safety.
 */
public class LazyVar<T> implements Supplier<T>{
	
	private final AtomicReference<T> ref = new AtomicReference<>();
	
	private final String name;
	
	public LazyVar(String name) {
		this.name = name;
	}

	@Override
	public T get() throws IllegalStateException{
		T result = getOptional();
		if(result==null) throw new IllegalStateException(format("{} has not been initialized."));
		return result;
	}

	public @Nullable T getOptional(){
		return ref.get();
	}

	public void set(T value) throws IllegalStateException{
		boolean success = ref.compareAndSet(null, value);
		if(!success) throw new IllegalStateException(format("{} has already been initialized."));
	}

	@Override
	public String toString() {
		return name;
	}
	
	

}
