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
import java.nio.channels.Channel;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Creates a close-shielding proxy for a {@link Channel}.
 *
 * <p>
 * The returned proxy will implement all {@link Channel} sub-interfaces that the delegate implements.
 * </p>
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
                if (Channel.class.isAssignableFrom(iface) && out.add(iface)) {
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
     * @return A proxy that shields {@code close()} and enforces closed semantics on other calls.
     */
    @SuppressWarnings({ "unchecked", "resource" }) // caller closes
    public static Channel wrap(final Channel channel) {
        Objects.requireNonNull(channel, "channel");
        // Fast path: already our shield
        if (Proxy.isProxyClass(channel.getClass()) && Proxy.getInvocationHandler(channel) instanceof CloseShieldChannelHandler) {
            return channel;
        }
        // Collect only Channel sub-interfaces.
        final Set<Class<?>> set = collectChannelInterfaces(channel.getClass(), new LinkedHashSet<>());
        // fallback to root surface
        return (Channel) Proxy.newProxyInstance(channel.getClass().getClassLoader(), // use delegate's loader
                set.isEmpty() ? new Class<?>[] { Channel.class } : set.toArray(EMPTY), new CloseShieldChannelHandler(channel));
    }

    /**
     * Wraps a channel to shield it from being closed.
     *
     * @param channel The underlying channel to shield, not {@code null} and must implement {@code type}.
     * @param type    The interface the returned proxy must implement;
     *                the proxy will also implement all other {@link Channel} sub-interfaces that the delegate implements.
     * @param <T>     A type that extends {@link Channel}.
     * @return A proxy that shields {@code close()} and enforces closed semantics on other calls.
     * @throws IllegalArgumentException if {@code type} is not an interface or if {@code channel} does not implement {@code type}.
     */
    @SuppressWarnings({ "unchecked", "resource" }) // caller closes
    public static <T extends Channel> T wrap(final T channel, Class<T> type) {
        Objects.requireNonNull(type, "type");
        if (!type.isInterface()) {
            throw new IllegalArgumentException(type.getName() + " is not an interface");
        }
        if (!type.isInstance(channel)) {
            throw new IllegalArgumentException("channel of type " + channel.getClass().getName() + " is not an instance of " + type.getName());
        }
        return type.cast(wrap(channel));
    }

    private CloseShieldChannel() {
        // no instance
    }
}
