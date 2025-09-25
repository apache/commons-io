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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.channels.Channel;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utility to create a close-shielding proxy for a {@link Channel}.
 *
 * <p>The returned proxy will implement all {@link Channel} sub-interfaces that the delegate implements.</p>
 *
 * @since 2.21.0
 */
public final class CloseShieldChannel {

    private CloseShieldChannel() {
        // no instance
    }

    /**
     * Wraps a channel to shield it from being closed.
     *
     * @param channel The underlying channel to shield, not {@code null}.
     * @param <T>     Any Channel type (interface or class).
     * @return A proxy that shields {@code close()} and enforces closed semantics on other calls.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Channel> T wrap(final T channel) {
        Objects.requireNonNull(channel, "channel");

        // Fast path: already our shield
        if (Proxy.isProxyClass(channel.getClass())) {
            final InvocationHandler handler = Proxy.getInvocationHandler(channel);
            if (handler instanceof CloseShieldChannelHandler) {
                return channel;
            }
        }

        // Collect only Channel sub-interfaces.
        Class<?>[] ifaces = collectChannelInterfaces(channel.getClass());
        if (ifaces.length == 0) {
            ifaces = new Class<?>[] {Channel.class}; // fallback to minimal surface
        }

        return (T) Proxy.newProxyInstance(
                channel.getClass().getClassLoader(), // use delegate's loader
                ifaces,
                new CloseShieldChannelHandler(channel));
    }

    private static Class<?>[] collectChannelInterfaces(final Class<?> type) {
        final Set<Class<?>> out = new LinkedHashSet<>();
        collectChannelInterfaces(type, out);
        return out.toArray(new Class<?>[0]);
    }

    private static void collectChannelInterfaces(final Class<?> type, final Set<Class<?>> out) {
        // Visit interfaces
        for (Class<?> iface : type.getInterfaces()) {
            if (Channel.class.isAssignableFrom(iface) && out.add(iface)) {
                collectChannelInterfaces(iface, out);
            }
        }
    }
}
