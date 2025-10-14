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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests {@link CloseShieldChannel}.
 */
class CloseShieldChannelTest {

    static Stream<Class<? extends Channel>> testedInterfaces() {
        // @formatter:off
        return Stream.of(
                AsynchronousChannel.class,
                ByteChannel.class,
                Channel.class,
                GatheringByteChannel.class,
                InterruptibleChannel.class,
                NetworkChannel.class,
                ReadableByteChannel.class,
                ScatteringByteChannel.class,
                SeekableByteChannel.class,
                WritableByteChannel.class);
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("testedInterfaces")
    void testCloseDoesNotCloseDelegate(final Class<? extends Channel> channelClass) throws Exception {
        final Channel channel = mock(channelClass);
        final Channel shield = CloseShieldChannel.wrap(channel);
        shield.close();
        verify(channel, never()).close();
    }

    @ParameterizedTest
    @MethodSource("testedInterfaces")
    void testCloseIsIdempotent(final Class<? extends Channel> channelClass) throws Exception {
        final Channel channel = mock(channelClass);
        final Channel shield = CloseShieldChannel.wrap(channel);
        shield.close();
        assertFalse(shield.isOpen());
        shield.close();
        assertFalse(shield.isOpen());
        verifyNoInteractions(channel);
    }

    @ParameterizedTest
    @MethodSource("testedInterfaces")
    void testCloseIsShielded(final Class<? extends Channel> channelClass) throws Exception {
        final Channel channel = mock(channelClass);
        when(channel.isOpen()).thenReturn(true, false, true, false);
        final Channel shield = CloseShieldChannel.wrap(channel);
        // Reflects delegate state initially
        assertTrue(shield.isOpen(), "isOpen reflects delegate state");
        assertFalse(shield.isOpen(), "isOpen reflects delegate state");
        verify(channel, times(2)).isOpen();
        shield.close();
        // Reflects shield state after close
        assertFalse(shield.isOpen(), "isOpen reflects shield state");
        assertFalse(shield.isOpen(), "isOpen reflects shield state");
        verify(channel, times(2)).isOpen();
    }

    @Test
    void testCorrectlyDetectsInterfaces(@TempDir Path tempDir) throws IOException {
        final Path testFile = tempDir.resolve("test.txt");
        FileUtils.touch(testFile.toFile());
        try (FileChannel channel = FileChannel.open(testFile); Channel shield = CloseShieldChannel.wrap(channel)) {
            assertInstanceOf(SeekableByteChannel.class, shield);
            assertInstanceOf(GatheringByteChannel.class, shield);
            assertInstanceOf(WritableByteChannel.class, shield);
            assertInstanceOf(ScatteringByteChannel.class, shield);
            assertInstanceOf(ReadableByteChannel.class, shield);
            assertInstanceOf(InterruptibleChannel.class, shield);
            assertInstanceOf(ByteChannel.class, shield);
            assertInstanceOf(Channel.class, shield);
            // These are not interfaces, so can not be implemented
            assertFalse(shield instanceof FileChannel, "not FileChannel");
        }
    }

    @Test
    void testDoesNotDoubleWrap() {
        final ByteChannel channel = mock(ByteChannel.class);
        final ByteChannel shield1 = CloseShieldChannel.wrap(channel);
        final ByteChannel shield2 = CloseShieldChannel.wrap(shield1);
        assertSame(shield1, shield2);
    }

    @ParameterizedTest
    @MethodSource("testedInterfaces")
    void testEquals(final Class<? extends Channel> channelClass) throws Exception {
        final Channel channel = mock(channelClass);
        final Channel shield = CloseShieldChannel.wrap(channel);
        final Channel anotherShield = CloseShieldChannel.wrap(channel);
        assertTrue(shield.equals(shield), "reflexive");
        assertFalse(shield.equals(null), "null is not equal");
        assertFalse(shield.equals(channel), "shield not equal to delegate");
        assertTrue(shield.equals(anotherShield), "shields of same delegate are equal");
    }

    @Test
    void testGatheringByteChannelMethods() throws Exception {
        final GatheringByteChannel channel = mock(GatheringByteChannel.class);
        when(channel.isOpen()).thenReturn(true);
        final GatheringByteChannel shield = (GatheringByteChannel) CloseShieldChannel.wrap(channel);
        // Before close write() should delegate
        when(channel.write(null, 0, 0)).thenReturn(42L);
        assertEquals(42, shield.write(null, 0, 0));
        verify(channel).write(null, 0, 0);
        // After close write() should throw ClosedChannelException
        shield.close();
        assertThrows(ClosedChannelException.class, () -> shield.write(null, 0, 0));
        verifyNoMoreInteractions(channel);
    }

    @ParameterizedTest
    @MethodSource("testedInterfaces")
    void testHashCode(final Class<? extends Channel> channelClass) throws Exception {
        final Channel channel = mock(channelClass);
        final Channel shield = CloseShieldChannel.wrap(channel);
        final Channel anotherShield = CloseShieldChannel.wrap(channel);
        assertEquals(shield.hashCode(), channel.hashCode(), "delegates hashCode");
        assertEquals(shield.hashCode(), anotherShield.hashCode(), "shields of same delegate have same hashCode");
    }

    @Test
    void testNetworkChannelMethods() throws Exception {
        final NetworkChannel channel = mock(NetworkChannel.class);
        when(channel.isOpen()).thenReturn(true);
        final NetworkChannel shield = (NetworkChannel) CloseShieldChannel.wrap(channel);
        // Before close getOption(), setOption(), getLocalAddress() and bind() should delegate
        when(channel.getOption(null)).thenReturn("foo");
        when(channel.setOption(null, null)).thenReturn(channel);
        when(channel.getLocalAddress()).thenReturn(null);
        when(channel.bind(null)).thenReturn(channel);
        assertEquals("foo", shield.getOption(null));
        assertEquals(shield, shield.setOption(null, null));
        assertEquals(null, shield.getLocalAddress());
        assertEquals(shield, shield.bind(null));
        verify(channel).getOption(null);
        verify(channel).setOption(null, null);
        verify(channel).getLocalAddress();
        verify(channel).bind(null);
        // After close supportedOptions() should still work
        shield.close();
        assertDoesNotThrow(shield::supportedOptions);
        verify(channel).supportedOptions();
        // But the remaining methods should throw ClosedChannelException
        assertThrows(ClosedChannelException.class, () -> shield.setOption(null, null));
        assertThrows(ClosedChannelException.class, () -> shield.getOption(null));
        assertThrows(ClosedChannelException.class, shield::getLocalAddress);
        assertThrows(ClosedChannelException.class, () -> shield.bind(null));
        verifyNoMoreInteractions(channel);
    }

    @ParameterizedTest
    @MethodSource("testedInterfaces")
    void testPreservesInterfaces(final Class<? extends Channel> channelClass) {
        final Channel channel = mock(channelClass);
        final Channel shield = CloseShieldChannel.wrap(channel);
        assertNotSame(channel, shield);
        assertTrue(channelClass.isInstance(shield));
    }

    @Test
    void testReadableByteChannelMethods() throws Exception {
        final ReadableByteChannel channel = mock(ReadableByteChannel.class);
        when(channel.isOpen()).thenReturn(true);
        final ReadableByteChannel shield = CloseShieldChannel.wrap(channel);
        // Before close read() should delegate
        when(channel.read(null)).thenReturn(42);
        assertEquals(42, shield.read(null));
        verify(channel).read(null);
        // After close read() should throw ClosedChannelException
        shield.close();
        assertThrows(ClosedChannelException.class, () -> shield.read(null));
        verifyNoMoreInteractions(channel);
    }

    @Test
    void testScatteringByteChannelMethods() throws Exception {
        final ScatteringByteChannel channel = mock(ScatteringByteChannel.class);
        when(channel.isOpen()).thenReturn(true);
        final ScatteringByteChannel shield = (ScatteringByteChannel) CloseShieldChannel.wrap(channel);
        // Before close read() should delegate
        when(channel.read(null, 0, 0)).thenReturn(42L);
        assertEquals(42, shield.read(null, 0, 0));
        verify(channel).read(null, 0, 0);
        // After close read() should throw ClosedChannelException
        shield.close();
        assertThrows(ClosedChannelException.class, () -> shield.read(null, 0, 0));
        verifyNoMoreInteractions(channel);
    }

    @Test
    void testSeekableByteChannelMethods() throws Exception {
        final SeekableByteChannel channel = mock(SeekableByteChannel.class);
        when(channel.isOpen()).thenReturn(true);
        final SeekableByteChannel shield = CloseShieldChannel.wrap(channel);
        // Before close position() and size() should delegate
        when(channel.position()).thenReturn(42L);
        when(channel.size()).thenReturn(84L);
        assertEquals(42, shield.position());
        assertEquals(84, shield.size());
        verify(channel).position();
        verify(channel).size();
        // Before close position(long) and truncate(long) should delegate
        when(channel.position(21)).thenReturn(channel);
        when(channel.truncate(21)).thenReturn(channel);
        assertEquals(shield, shield.position(21));
        assertEquals(shield, shield.truncate(21));
        verify(channel).position(21);
        verify(channel).truncate(21);
        // After close position() should throw ClosedChannelException
        shield.close();
        assertThrows(ClosedChannelException.class, shield::position);
        assertThrows(ClosedChannelException.class, () -> shield.position(0));
        assertThrows(ClosedChannelException.class, shield::size);
        assertThrows(ClosedChannelException.class, () -> shield.truncate(0));
        verifyNoMoreInteractions(channel);
    }

    @ParameterizedTest
    @MethodSource("testedInterfaces")
    void testToString(final Class<? extends Channel> channelClass) throws Exception {
        final Channel channel = mock(channelClass);
        when(channel.toString()).thenReturn("MyChannel");
        final Channel shield = CloseShieldChannel.wrap(channel);
        final String shieldString = shield.toString();
        assertTrue(shieldString.contains("CloseShield"));
        assertTrue(shieldString.contains("MyChannel"));
    }

    @Test
    void testWritableByteChannelMethods() throws Exception {
        final WritableByteChannel channel = mock(WritableByteChannel.class);
        when(channel.isOpen()).thenReturn(true);
        final WritableByteChannel shield = CloseShieldChannel.wrap(channel);
        // Before close write() should delegate
        when(channel.write(null)).thenReturn(42);
        assertEquals(42, shield.write(null));
        verify(channel).write(null);
        // After close write() should throw ClosedChannelException
        shield.close();
        assertThrows(ClosedChannelException.class, () -> shield.write(null));
        verifyNoMoreInteractions(channel);
    }
}
