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

import java.io.Closeable;
import java.lang.reflect.Proxy;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Creates a close-shielding proxy for a {@link Channel}.
 *
 * <p>The returned proxy implements all {@link Channel} sub-interfaces that are both supported by this implementation and actually implemented by the given
 * delegate.</p>
 *
 * <p>The following interfaces are supported:</p>
 *
 * <ul>
 * <li>{@link AsynchronousChannel}</li>
 * <li>{@link ByteChannel}</li>
 * <li>{@link Channel}</li>
 * <li>{@link GatheringByteChannel}</li>
 * <li>{@link InterruptibleChannel}</li>
 * <li>{@link NetworkChannel}</li>
 * <li>{@link ReadableByteChannel}</li>
 * <li>{@link ScatteringByteChannel}</li>
 * <li>{@link SeekableByteChannel}</li>
 * <li>{@link WritableByteChannel}</li>
 * </ul>
 *
 * @see Channel
 * @see Closeable
 * @since 2.21.0
 */
public final class CloseShieldChannel {

    private static final Class<?>[] EMPTY = {};

    private static Set<Class<?>> collectChannelInterfaces(final Class<?> type, final Set<Class<?>> out) {
        Class<?> currentType = type;
        // Visit interfaces
        while (currentType != null) {
            for (final Class<?> iface : currentType.getInterfaces()) {
                if (CloseShieldChannelHandler.isSupported(iface) && out.add(iface)) {
                    collectChannelInterfaces(iface, out);
                }
            }
            currentType = currentType.getSuperclass();
        }
        return out;
    }

    /**
     * Wraps a channel to shield it from being closed.
     *
     * @param channel The underlying channel to shield, not {@code null}.
     * @param <T>     A supported channel type.
     * @return A proxy that shields {@code close()} and enforces closed semantics on other calls.
     * @throws ClassCastException if {@code T} is not a supported channel type.
     * @throws NullPointerException if {@code channel} is {@code null}.
     */
    @SuppressWarnings({ "unchecked", "resource" }) // caller closes
    public static <T extends Channel> T wrap(final T channel) {
        Objects.requireNonNull(channel, "channel");
        // Fast path: already our shield
        if (Proxy.isProxyClass(channel.getClass()) && Proxy.getInvocationHandler(channel) instanceof CloseShieldChannelHandler) {
            return channel;
        }
        // Collect only Channel sub-interfaces.
        final Set<Class<?>> set = collectChannelInterfaces(channel.getClass(), new LinkedHashSet<>());
        // fallback to root surface
        return (T) Proxy.newProxyInstance(channel.getClass().getClassLoader(), // use delegate's loader
                set.isEmpty() ? new Class<?>[] { Channel.class } : set.toArray(EMPTY), new CloseShieldChannelHandler(channel));
    }

    private CloseShieldChannel() {
        // no instance
    }
}
