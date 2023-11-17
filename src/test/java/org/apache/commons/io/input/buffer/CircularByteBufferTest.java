package org.apache.commons.io.input.buffer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CircularByteBufferTest {
	
    // Tests for add function with 3 arguments of type byte[], int and int
    @Test
    public void testAddValidData() {
    	CircularByteBuffer cbb = new CircularByteBuffer();
        int length = 3;
        int offset = 0;
        byte[] targetBuffer = new byte[] { 3, 6, 9 };
        cbb.add(targetBuffer, offset, length);
        assertEquals(length, cbb.getCurrentNumberOfBytes());
    }

    @Test
    public void testAddNegativeLength() {
    	CircularByteBuffer cbb = new CircularByteBuffer();
        int length = -1;
        int offset = 0;
        byte[] targetBuffer = new byte[] { 1, 2, 3 };
        assertThrows(IllegalArgumentException.class, () -> {
        	cbb.add(targetBuffer, offset, length);
        });
    }

    @Test
    public void testAddInvalidOffset() {
    	CircularByteBuffer cbb = new CircularByteBuffer();
        int length = 3;
        int offset = -1;
        byte[] targetBuffer = new byte[] { 1, 2, 3 };
        assertThrows(IllegalArgumentException.class, () -> {
        	cbb.add(targetBuffer, offset, length);
        });
    }
    
    @Test
    public void testAddNullBuffer() {
    	CircularByteBuffer cbb = new CircularByteBuffer();
        int length = 3;
        int offset = 0;
        byte[] targetBuffer = null;
        assertThrows(NullPointerException.class, () -> {
        	cbb.add(targetBuffer, offset, length);
        });
    }
  
    
    // Tests for peek function
    @Test
    public void testPeekWithValidArguments() {
        int length = 5;
        int offset = 0;
        byte[] buffer = { 5, 10, 15, 20, 25, 10, 15, 20, 25 };
        byte[] sourceBuffer = { 5, 10, 15, 20, 25 };
        CircularByteBuffer cbb = new CircularByteBuffer();
        assertFalse(cbb.peek(sourceBuffer, offset, length));
    }

    @Test
    public void testPeekWithNegativeLength() {
        int length = -1;
        int offset = 0;
        byte[] buffer = { 1, 4, 3, 1, 4, 3, 1, 4, 3 };
        byte[] sourceBuffer = { 1, 4, 3 };
        CircularByteBuffer cbb = new CircularByteBuffer();
        try {
        	cbb.peek(sourceBuffer, offset, length);
        } catch (IllegalArgumentException e) {
        	assertEquals("Illegal length: -1", e.getMessage());
        }
    }

    @Test
    public void testPeekWithExcessiveLength() {
        int length = 6;
        int offset = 0;
        byte[] buffer = { 1, 3, 5, 7, 9, 1, 3, 5, 7, 9 };
        byte[] sourceBuffer = { 1, 3, 5, 7, 9 };
        CircularByteBuffer cbb = new CircularByteBuffer();
        assertFalse(cbb.peek(sourceBuffer, offset, length));
    }
    
    @Test
    public void testPeekWithInvalidOffset() {
    	int length = 5;
    	int offset = -1;
        byte[] buffer = { 2, 4, 6, 8, 10, 2, 4, 6, 8, 10 };
        byte[] sourceBuffer = { 2, 4, 6, 8, 10 };
        CircularByteBuffer cbb = new CircularByteBuffer();
        try {
        	cbb.peek(sourceBuffer, offset, length);
        } catch (IllegalArgumentException e) {
        	assertEquals("Illegal offset: -1", e.getMessage());
        }
    }
}
