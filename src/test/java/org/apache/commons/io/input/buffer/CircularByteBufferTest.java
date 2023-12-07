package org.apache.commons.io.input.buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link CircularByteBuffer}.
 */
public class CircularByteBufferTest {

    @Test
    public void testAddInvalidOffset() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        assertThrows(IllegalArgumentException.class, () -> cbb.add(new byte[] { 1, 2, 3 }, -1, 3));
    }

    @Test
    public void testAddNegativeLength() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        final byte[] targetBuffer = { 1, 2, 3 };
        assertThrows(IllegalArgumentException.class, () -> cbb.add(targetBuffer, 0, -1));
    }

    @Test
    public void testAddNullBuffer() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        assertThrows(NullPointerException.class, () -> cbb.add(null, 0, 3));
    }

    /**
     * Tests for add function with 3 arguments of type byte[], int and int.
     */
    @Test
    public void testAddValidData() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        final int length = 3;
        cbb.add(new byte[] { 3, 6, 9 }, 0, length);
        assertEquals(length, cbb.getCurrentNumberOfBytes());
    }

    @Test
    public void testPeekWithExcessiveLength() {
        assertFalse(new CircularByteBuffer().peek(new byte[] { 1, 3, 5, 7, 9 }, 0, 6));
    }

    @Test
    public void testPeekWithInvalidOffset() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> cbb.peek(new byte[] { 2, 4, 6, 8, 10 }, -1, 5));
        assertEquals("Illegal offset: -1", e.getMessage());
    }

    @Test
    public void testPeekWithNegativeLength() {
        final CircularByteBuffer cbb = new CircularByteBuffer();
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> cbb.peek(new byte[] { 1, 4, 3 }, 0, -1));
        assertEquals("Illegal length: -1", e.getMessage());
    }

    // Tests for peek function
    @Test
    public void testPeekWithValidArguments() {
        assertFalse(new CircularByteBuffer().peek(new byte[] { 5, 10, 15, 20, 25 }, 0, 5));
    }
}
