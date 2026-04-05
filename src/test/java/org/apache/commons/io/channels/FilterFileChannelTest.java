/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.channels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link FilterFileChannel}.
 */
class FilterFileChannelTest {

    private FilterFileChannel build(final FileChannel channel) {
        return new FilterFileChannel(channel);
    }

    private FileChannel mockFileChannel() {
        return mock(FileChannel.class);
    }

    @Test
    void testConstructorRequiresNonNullChannel() {
        assertThrows(NullPointerException.class, () -> new FilterFileChannel(null));
    }

    @Test
    void testEqualsDelegatesToChannel() {
        final FileChannel channel = mockFileChannel();
        final FilterFileChannel filter = build(channel);
        // equals() delegates to the underlying channel. The mock returns false by default,
        // so filter.equals(anything) should be false.
        final Object other = new Object();
        assertEquals(channel.equals(other), filter.equals(other));
    }

    @Test
    void testForceDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        final FilterFileChannel filter = build(channel);
        filter.force(true);
        verify(channel).force(true);
    }

    @Test
    void testForcePropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        final FilterFileChannel filter = build(channel);
        // IOException is checked via implCloseChannel; use force directly
        final IOException ex = new IOException("force error");
        org.mockito.Mockito.doThrow(ex).when(channel).force(false);
        assertThrows(IOException.class, () -> filter.force(false));
        verify(channel).force(false);
    }

    @Test
    void testHashCodeDelegatesToChannel() {
        final FileChannel channel = mockFileChannel();
        final FilterFileChannel filter = build(channel);
        // hashCode() delegates to the underlying channel; both should return the same value.
        assertEquals(channel.hashCode(), filter.hashCode());
    }

    @Test
    void testImplCloseChannelDelegatesToChannelClose() throws IOException {
        final FileChannel channel = mockFileChannel();
        final FilterFileChannel filter = build(channel);
        filter.close();
        verify(channel).close();
    }

    @Test
    void testImplementsFileChannel() {
        final FileChannel channel = mockFileChannel();
        assertInstanceOf(FileChannel.class, build(channel));
    }

    @Test
    void testLockDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        final FileLock lock = mock(FileLock.class);
        when(channel.lock(0L, Long.MAX_VALUE, false)).thenReturn(lock);
        final FilterFileChannel filter = build(channel);
        assertSame(lock, filter.lock(0L, Long.MAX_VALUE, false));
        verify(channel).lock(0L, Long.MAX_VALUE, false);
    }

    @Test
    void testLockPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.lock(0L, Long.MAX_VALUE, false)).thenThrow(new IOException("lock error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.lock(0L, Long.MAX_VALUE, false));
        verify(channel).lock(0L, Long.MAX_VALUE, false);
    }

    @Test
    void testMapDelegatesToChannel() throws IOException {
        final Path tmp = Files.createTempFile("FilterFileChannelTest", ".bin");
        try {
            Files.write(tmp, new byte[1024]);
            try (FileChannel real = FileChannel.open(tmp, StandardOpenOption.READ); FilterFileChannel filter = build(real)) {
                final MappedByteBuffer mapped = filter.map(MapMode.READ_ONLY, 0L, 1024L);
                assertNotNull(mapped);
                assertEquals(1024, mapped.capacity());
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void testMapPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.map(MapMode.READ_ONLY, 0L, 1024L)).thenThrow(new IOException("map error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.map(MapMode.READ_ONLY, 0L, 1024L));
        verify(channel).map(MapMode.READ_ONLY, 0L, 1024L);
    }

    @Test
    void testPositionDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.position()).thenReturn(42L);
        final FilterFileChannel filter = build(channel);
        assertEquals(42L, filter.position());
        verify(channel).position();
    }

    @Test
    void testPositionPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.position()).thenThrow(new IOException("position error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, filter::position);
        verify(channel).position();
    }

    @Test
    void testReadAtPositionDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer, 10L)).thenReturn(8);
        final FilterFileChannel filter = build(channel);
        assertEquals(8, filter.read(buffer, 10L));
        verify(channel).read(buffer, 10L);
    }

    @Test
    void testReadAtPositionPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer, 10L)).thenThrow(new IOException("read error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.read(buffer, 10L));
        verify(channel).read(buffer, 10L);
    }
    // -- read(ByteBuffer) --

    @Test
    void testReadDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenReturn(8);
        final FilterFileChannel filter = build(channel);
        assertEquals(8, filter.read(buffer));
        verify(channel).read(buffer);
    }

    @Test
    void testReadPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenThrow(new IOException("read error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.read(buffer));
        verify(channel).read(buffer);
    }

    @Test
    void testReadReturnsMinusOneAtEndOfStream() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.read(buffer)).thenReturn(-1);
        final FilterFileChannel filter = build(channel);
        assertEquals(-1, filter.read(buffer));
        verify(channel).read(buffer);
    }

    @Test
    void testReadScatteringDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer[] buffers = { ByteBuffer.allocate(8), ByteBuffer.allocate(8) };
        when(channel.read(buffers, 0, 2)).thenReturn(16L);
        final FilterFileChannel filter = build(channel);
        assertEquals(16L, filter.read(buffers, 0, 2));
        verify(channel).read(buffers, 0, 2);
    }

    @Test
    void testReadScatteringPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer[] buffers = { ByteBuffer.allocate(8), ByteBuffer.allocate(8) };
        when(channel.read(buffers, 0, 2)).thenThrow(new IOException("scatter read error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.read(buffers, 0, 2));
        verify(channel).read(buffers, 0, 2);
    }

    @Test
    void testSetPositionDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.position(100L)).thenReturn(channel);
        final FilterFileChannel filter = build(channel);
        assertSame(channel, filter.position(100L));
        verify(channel).position(100L);
    }

    @Test
    void testSetPositionPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.position(100L)).thenThrow(new IOException("position error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.position(100L));
        verify(channel).position(100L);
    }

    @Test
    void testSizeDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.size()).thenReturn(2048L);
        final FilterFileChannel filter = build(channel);
        assertEquals(2048L, filter.size());
        verify(channel).size();
    }

    @Test
    void testSizePropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.size()).thenThrow(new IOException("size error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, filter::size);
        verify(channel).size();
    }

    @Test
    void testToStringDelegatesToChannel() {
        final FileChannel channel = mockFileChannel();
        when(channel.toString()).thenReturn("mockChannel");
        final FilterFileChannel filter = build(channel);
        assertEquals("mockChannel", filter.toString());
    }

    @Test
    void testTransferFromDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ReadableByteChannel src = mock(ReadableByteChannel.class);
        when(channel.transferFrom(src, 0L, 512L)).thenReturn(512L);
        final FilterFileChannel filter = build(channel);
        assertEquals(512L, filter.transferFrom(src, 0L, 512L));
        verify(channel).transferFrom(src, 0L, 512L);
    }

    @Test
    void testTransferFromPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ReadableByteChannel src = mock(ReadableByteChannel.class);
        when(channel.transferFrom(src, 0L, 512L)).thenThrow(new IOException("transferFrom error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.transferFrom(src, 0L, 512L));
        verify(channel).transferFrom(src, 0L, 512L);
    }

    @Test
    void testTransferToDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        final WritableByteChannel target = mock(WritableByteChannel.class);
        when(channel.transferTo(0L, 512L, target)).thenReturn(512L);
        final FilterFileChannel filter = build(channel);
        assertEquals(512L, filter.transferTo(0L, 512L, target));
        verify(channel).transferTo(0L, 512L, target);
    }

    @Test
    void testTransferToPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        final WritableByteChannel target = mock(WritableByteChannel.class);
        when(channel.transferTo(0L, 512L, target)).thenThrow(new IOException("transferTo error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.transferTo(0L, 512L, target));
        verify(channel).transferTo(0L, 512L, target);
    }

    @Test
    void testTruncateDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.truncate(512L)).thenReturn(channel);
        final FilterFileChannel filter = build(channel);
        assertSame(channel, filter.truncate(512L));
        verify(channel).truncate(512L);
    }

    @Test
    void testTruncatePropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.truncate(512L)).thenThrow(new IOException("truncate error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.truncate(512L));
        verify(channel).truncate(512L);
    }

    @Test
    void testTryLockDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        final FileLock lock = mock(FileLock.class);
        when(channel.tryLock(0L, Long.MAX_VALUE, false)).thenReturn(lock);
        final FilterFileChannel filter = build(channel);
        assertSame(lock, filter.tryLock(0L, Long.MAX_VALUE, false));
        verify(channel).tryLock(0L, Long.MAX_VALUE, false);
    }

    @Test
    void testTryLockPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.tryLock(0L, Long.MAX_VALUE, false)).thenThrow(new IOException("tryLock error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.tryLock(0L, Long.MAX_VALUE, false));
        verify(channel).tryLock(0L, Long.MAX_VALUE, false);
    }

    @Test
    void testTryLockReturnsNullWhenLockNotAcquired() throws IOException {
        final FileChannel channel = mockFileChannel();
        when(channel.tryLock(0L, Long.MAX_VALUE, false)).thenReturn(null);
        final FilterFileChannel filter = build(channel);
        assertEquals(null, filter.tryLock(0L, Long.MAX_VALUE, false));
        verify(channel).tryLock(0L, Long.MAX_VALUE, false);
    }

    @Test
    void testUnwrapIsNotNull() {
        assertNotNull(build(mockFileChannel()).unwrap());
    }

    @Test
    void testUnwrapReturnsWrappedChannel() {
        final FileChannel channel = mockFileChannel();
        final FilterFileChannel filter = build(channel);
        assertSame(channel, filter.unwrap());
    }

    @Test
    void testWriteAtPositionDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.write(buffer, 10L)).thenReturn(16);
        final FilterFileChannel filter = build(channel);
        assertEquals(16, filter.write(buffer, 10L));
        verify(channel).write(buffer, 10L);
    }

    @Test
    void testWriteAtPositionPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.write(buffer, 10L)).thenThrow(new IOException("write error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.write(buffer, 10L));
        verify(channel).write(buffer, 10L);
    }

    @Test
    void testWriteDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.write(buffer)).thenReturn(16);
        final FilterFileChannel filter = build(channel);
        assertEquals(16, filter.write(buffer));
        verify(channel).write(buffer);
    }

    @Test
    void testWriteGatheringDelegatesToChannel() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer[] buffers = { ByteBuffer.allocate(8), ByteBuffer.allocate(8) };
        when(channel.write(buffers, 0, 2)).thenReturn(16L);
        final FilterFileChannel filter = build(channel);
        assertEquals(16L, filter.write(buffers, 0, 2));
        verify(channel).write(buffers, 0, 2);
    }

    @Test
    void testWriteGatheringPropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer[] buffers = { ByteBuffer.allocate(8), ByteBuffer.allocate(8) };
        when(channel.write(buffers, 0, 2)).thenThrow(new IOException("gather write error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.write(buffers, 0, 2));
        verify(channel).write(buffers, 0, 2);
    }

    @Test
    void testWritePropagatesIOException() throws IOException {
        final FileChannel channel = mockFileChannel();
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        when(channel.write(buffer)).thenThrow(new IOException("write error"));
        final FilterFileChannel filter = build(channel);
        assertThrows(IOException.class, () -> filter.write(buffer));
        verify(channel).write(buffer);
    }
}
