/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.input.buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;


public class CircularBufferInputStreamTest {
	private final Random rnd = new Random(1530960934483L); // System.currentTimeMillis(), when this test was written.
	                                                       // Always using the same seed should ensure a reproducable test.

	@Test
	public void testRandomRead() throws Exception {
		final byte[] inputBuffer = newInputBuffer();
		final byte[] bufferCopy = new byte[inputBuffer.length];
		final ByteArrayInputStream bais = new ByteArrayInputStream(inputBuffer);
		@SuppressWarnings("resource")
		final CircularBufferInputStream cbis = new CircularBufferInputStream(bais, 253);
		int offset = 0;
		final byte[] readBuffer = new byte[256];
		while (offset < bufferCopy.length) {
			switch (rnd.nextInt(2)) {
			case 0:
			{
				final int res = cbis.read();
				if (res == -1) {
					throw new IllegalStateException("Unexpected EOF at offset " + offset);
				}
				if (inputBuffer[offset] != (byte) res) { // compare as bytes
					throw new IllegalStateException("Expected " + inputBuffer[offset] + " at offset " + offset + ", got " + res);
				}
				++offset;
				break;
			}
			case 1:
			{
				final int res = cbis.read(readBuffer, 0, rnd.nextInt(readBuffer.length+1));
				if (res == -1) {
					throw new IllegalStateException("Unexpected EOF at offset " + offset);
				} else if (res == 0) {
					throw new IllegalStateException("Unexpected zero-byte-result at offset " + offset);
				} else {
					for (int i = 0;  i < res;  i++) {
						if (inputBuffer[offset] != readBuffer[i]) {
							throw new IllegalStateException("Expected " + inputBuffer[offset] + " at offset " + offset + ", got " + readBuffer[i]);
						}
						++offset;
					}
				}
				break;
			}
			default:
				throw new IllegalStateException("Unexpected random choice value");
			}
		}
		assertTrue(true, "Test finished OK");
	}

	@Test
  public void testIO683() throws IOException {
		final byte[] buffer = new byte[]{0,1,-2,-2,-1,4};
		try (
			final ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
			final CircularBufferInputStream cbis = new CircularBufferInputStream(bais)
		){
			int b;
			int i = 0;
			while((b = cbis.read()) != -1) {
				assertEquals(buffer[i] & 0xFF,b, "byte at index " + i + " should be equal");
				i++;
			}
			assertEquals(buffer.length, i, "Should have read all the bytes");
		}
	}

	/**
	 * Create a large, but random input buffer.
	 */
	private byte[] newInputBuffer() {
		final byte[] buffer = new byte[16*512+rnd.nextInt(512)];
		rnd.nextBytes(buffer);
		return buffer;
	}
}
