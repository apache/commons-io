package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class InfiniteCircularInputStreamTest {

	@Test
	public void should_cycle_bytes() throws IOException {
		byte[] input    = new byte[]{1,2};
		byte[] expected = new byte[]{1,2,1,2,1};
		
		assertStreamOutput(input, expected);
	}
	
	@Test
	public void should_handle_whole_range_of_bytes() throws IOException {
		int size = Byte.MAX_VALUE - Byte.MIN_VALUE + 1;
		byte[] contentToCycle = new byte[size];
		byte value = Byte.MIN_VALUE;
		for (int i = 0; i < contentToCycle.length; i++) {
			contentToCycle[i] = value++;
		}
		
		byte[] expectedOutput = Arrays.copyOf(contentToCycle, size);
		
		assertStreamOutput(contentToCycle, expectedOutput);
	}
	
	private void assertStreamOutput(byte[] toCycle, byte[] expected) throws IOException {
		byte[] actual = new byte[expected.length];

		InputStream infStream = new InfiniteCircularInputStream(toCycle);
		int actualReadBytes = infStream.read(actual);
		
		Assert.assertArrayEquals(expected, actual);
		Assert.assertEquals(expected.length, actualReadBytes);
	}
	
}
