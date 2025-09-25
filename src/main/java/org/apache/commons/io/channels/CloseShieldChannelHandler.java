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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

final class CloseShieldChannelHandler implements InvocationHandler {

    /**
     * Tests whether the given method is allowed to be called after the shield is closed.
     *
     * @param declaringClass The class declaring the method.
     * @param name           The method name.
     * @param parameterCount The number of parameters.
     * @return {@code true} if the method is allowed after {@code close()}, {@code false} otherwise.
     */
    private static boolean isAllowedAfterClose(final Class<?> declaringClass, final String name, final int parameterCount) {
        // JDK explicitly allows NetworkChannel.supportedOptions() post-close
        return parameterCount == 0 && name.equals("supportedOptions") && NetworkChannel.class.equals(declaringClass);
    }

    /**
     * Tests whether the given method returns 'this' (the channel) as per JDK spec.
     *
     * @param declaringClass The class declaring the method.
     * @param name           The method name.
     * @param parameterCount The number of parameters.
     * @return {@code true} if the method returns 'this', {@code false} otherwise.
     */
    private static boolean returnsThis(final Class<?> declaringClass, final String name, final int parameterCount) {
        if (SeekableByteChannel.class.equals(declaringClass)) {
            // SeekableByteChannel.position(long) and truncate(long) return 'this'
            return parameterCount == 1 && (name.equals("position") || name.equals("truncate"));
        }
        if (NetworkChannel.class.equals(declaringClass)) {
            // NetworkChannel.bind and NetworkChannel.setOption returns 'this'
            return parameterCount == 1 && name.equals("bind") || parameterCount == 2 && name.equals("setOption");
        }
        return false;
    }

    private final Channel delegate;
    private volatile boolean closed;

    CloseShieldChannelHandler(final Channel delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Class<?> declaringClass = method.getDeclaringClass();
        final String name = method.getName();
        final int parameterCount = method.getParameterCount();
        // 1) java.lang.Object methods
        if (declaringClass == Object.class) {
            return invokeObjectMethod(proxy, method, args);
        }
        // 2) Channel.close(): mark shield closed, do NOT close the delegate
        if (parameterCount == 0 && name.equals("close")) {
            closed = true;
            return null;
        }
        // 3) Channel.isOpen(): reflect shield state only
        if (parameterCount == 0 && name.equals("isOpen")) {
            return !closed && delegate.isOpen();
        }
        // 4) After the shield is closed, only allow a tiny allowlist of safe queries
        if (closed && !isAllowedAfterClose(declaringClass, name, parameterCount)) {
            throw new ClosedChannelException();
        }
        // 5) Delegate to the underlying channel and unwrap target exceptions
        try {
            final Object result = method.invoke(delegate, args);
            return returnsThis(declaringClass, name, parameterCount) ? proxy : result;
        } catch (final InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private Object invokeObjectMethod(final Object proxy, final Method method, final Object[] args) {
        switch (method.getName()) {
        case "toString":
            return "CloseShieldChannel(" + delegate + ")";
        case "hashCode":
            return Objects.hashCode(delegate);
        case "equals": {
            final Object other = args[0];
            if (other == null) {
                return false;
            }
            if (proxy == other) {
                return true;
            }
            if (Proxy.isProxyClass(other.getClass())) {
                final InvocationHandler h = Proxy.getInvocationHandler(other);
                if (h instanceof CloseShieldChannelHandler) {
                    return Objects.equals(((CloseShieldChannelHandler) h).delegate, this.delegate);
                }
            }
            return false;
        }
        default:
            // Not possible, all non-final Object methods are handled above
            return null;
        }
    }
}
