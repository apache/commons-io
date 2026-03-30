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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.channels.Channel;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link FilterChannel}.
 */
class FilterChannelTest {

    private FilterChannel<Channel> buildFilterChannel(final Channel channel) throws IOException {
        return FilterChannel.forChannel().setChannel(channel).get();
    }

    @Test
    void testBuilderRequiresChannel() {
        assertThrows(IllegalStateException.class, () -> FilterChannel.forChannel().get());
    }

    @Test
    void testClose() throws IOException {
        final Channel channel = mock(Channel.class);
        final FilterChannel<Channel> filterChannel = buildFilterChannel(channel);
        filterChannel.close();
        verify(channel).close();
    }

    @Test
    void testCloseClosesUnderlyingChannel() throws IOException {
        final Channel channel = mock(Channel.class);
        when(channel.isOpen()).thenReturn(true);
        final FilterChannel<Channel> filterChannel = buildFilterChannel(channel);
        assertTrue(filterChannel.isOpen());
        filterChannel.close();
        verify(channel).close();
    }

    @Test
    void testIsOpenAfterClose() throws IOException {
        final Channel channel = mock(Channel.class);
        // Simulate the channel reporting open=true then open=false after close
        when(channel.isOpen()).thenReturn(true).thenReturn(false);
        final FilterChannel<Channel> filterChannel = buildFilterChannel(channel);
        assertTrue(filterChannel.isOpen());
        filterChannel.close();
        verify(channel).close();
        assertFalse(filterChannel.isOpen());
    }

    @Test
    void testIsOpenDelegatesToChannel() throws IOException {
        final Channel channel = mock(Channel.class);
        when(channel.isOpen()).thenReturn(true, false);
        final FilterChannel<Channel> filterChannel = buildFilterChannel(channel);
        assertTrue(filterChannel.isOpen());
        assertFalse(filterChannel.isOpen());
        verify(channel, org.mockito.Mockito.times(2)).isOpen();
    }

    @Test
    void testUnwrapReturnsWrappedChannel() throws IOException {
        final Channel channel = mock(Channel.class);
        final FilterChannel<Channel> filterChannel = buildFilterChannel(channel);
        assertSame(channel, filterChannel.unwrap());
    }
}
