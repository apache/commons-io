/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.io.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.BoundedInputStream;

/**
 * An {@link ObjectInputStream} that's restricted to deserialize a limited set of classes.
 *
 * <p>
 * Various accept/reject methods allow for specifying which classes can be deserialized.
 * </p>
 * <h2>Deserializing with class restrictions</h2>
 * <p>
 * This example restricts deserialization to the classes used by a HashMap of String keys and Integer values. Class restrictions alone do not bound
 * resource use; see the resource limits below:
 * </p>
 *
 * <pre>{@code
 * // Defining Object fixture
 * final HashMap<String, Integer> map1 = new HashMap<>();
 * map1.put("1", 1);
 * // Writing serialized fixture
 * final byte[] byteArray;
 * try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
 *         final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
 *     oos.writeObject(map1);
 *     oos.flush();
 *     byteArray = baos.toByteArray();
 * }
 * // Deserializing
 * try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
 *         ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
 *             .accept(HashMap.class, Number.class, Integer.class)
 *             .setInputStream(bais)
 *             .get()) {
 *     // String.class is automatically accepted
 *     final HashMap<String, Integer> map2 = (HashMap<String, Integer>) vois.readObject();
 *     assertEquals(map1, map2);
 * }
 * // Reusing a configuration
 * final ObjectStreamClassPredicate predicate = new ObjectStreamClassPredicate()
 *     .accept(HashMap.class, Number.class, Integer.class);
 * try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
 *         ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
 *             .setPredicate(predicate)
 *             .setInputStream(bais)
 *             .get()) {
 *     // String.class is automatically accepted
 *     final HashMap<String, Integer> map2 = (HashMap<String, Integer>) vois.readObject();
 *     assertEquals(map1, map2);
 * }
 * }</pre>
 * <p>
 * This design was inspired by a <a href="https://www.ibm.com/developerworks/library/se-lookahead/">IBM DeveloperWorks Article</a>.
 * </p>
 * <h2>Deserializing with a size boundary</h2>
 * <p>
 * You can further guard your application against untrusted input by limiting how much data to process using a {@link BoundedInputStream}.
 * Note that a byte-count bound alone does not bound allocation, nesting depth, or reference count claimed by the bytes already read; see
 * <em>Deserializing with resource limits</em> below. For example:
 * </p>
 * <pre>{@code
 * // Deserializing with a size limit successfully
 * try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
 *         ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
 *             .accept(HashMap.class, Number.class, Integer.class)
 *             .setInputStream(BoundedInputStream.builder()
 *                 .setMaxCount(10_000)
 *                 .setOnMaxCount((max, count) -> {
 *                     throw new IllegalArgumentException("Input exceeds limit.");
 *                 })
 *                 .setInputStream(bais)
 *                 .get())
 *             .get()) {
 *     // String.class is automatically accepted
 *     final HashMap<String, Integer> map2 = (HashMap<String, Integer>) vois.readObject();
 *     assertEquals(map1, map2);
 * }
 * // Deserializing with a size limit reaching the limit
 * try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
 *         ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
 *             .accept(HashMap.class, Number.class, Integer.class)
 *             .setInputStream(BoundedInputStream.builder()
 *                 .setMaxCount(10)
 *                 .setOnMaxCount((max, count) -> {
 *                     throw new IllegalArgumentException("Input exceeds limit.");
 *                 })
 *                 .setInputStream(bais)
 *                 .get())
 *             .get()) {
 *     // String.class is automatically accepted
 *     final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> vois.readObject());
 *     assertEquals("Input exceeds limit.", e.getMessage());
 * }
 * }</pre>
 * <h2>Deserializing with resource limits</h2>
 * <p>
 * Validating class names, or bounding the input byte count as above, does <em>not</em> bound resources that a serialized stream can claim without actually
 * sending the corresponding bytes: a stream of a few dozen bytes can declare a huge array (which is allocated <em>before</em> its contents are read), nest
 * accepted containers deeply enough to exhaust the call stack with a {@link StackOverflowError}, or multiply the object graph through internal references.
 * To also bound these resources, set the <a href="https://openjdk.org/jeps/290">JEP 290</a>-style limits {@link Builder#setMaxDepth(long)},
 * {@link Builder#setMaxArrayLength(long)} and {@link Builder#setMaxRefs(long)}, which this class enforces through the platform {@code ObjectInputFilter}:
 * </p>
 * <pre>{@code
 * try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
 *         ValidatingObjectInputStream vois = ValidatingObjectInputStream.builder()
 *             .accept(HashMap.class, Number.class, Integer.class)
 *             .setMaxDepth(100)          // maximum nesting depth of the object graph
 *             .setMaxArrayLength(10_000) // maximum declared array length
 *             .setMaxRefs(10_000)        // maximum number of internal object references
 *             .setInputStream(bais)
 *             .get()) {
 *     // String.class is automatically accepted
 *     final HashMap<String, Integer> map2 = (HashMap<String, Integer>) vois.readObject();
 *     assertEquals(map1, map2);
 * }
 * }</pre>
 * <p>
 * For streams that may be crafted by an adversary, combine all three guards: an accept list, a {@link BoundedInputStream} byte bound, and the resource limits
 * above. These limits do not guarantee safe deserialization of arbitrary untrusted data. Existing stream filter rejections are preserved; failures
 * to install the limits are propagated to the caller.
 * </p>
 * @since 2.5
 */
public class ValidatingObjectInputStream extends ObjectInputStream {

    // @formatter:off
    /**
     * Builds a new {@link ValidatingObjectInputStream}.
     *
     * <h2>Using NIO</h2>
     * <pre>{@code
     * ValidatingObjectInputStream s = ValidatingObjectInputStream.builder()
     *   .setPath(Paths.get("MyFile.ser"))
     *   .get();}
     * </pre>
     * <h2>Using IO</h2>
     * <pre>{@code
     * ValidatingObjectInputStream s = ValidatingObjectInputStream.builder()
     *   .setFile(new File("MyFile.ser"))
     *   .get();}
     * </pre>
     *
     * @see #get()
     * @since 2.18.0
     */
    // @formatter:on
    public static class Builder extends AbstractStreamBuilder<ValidatingObjectInputStream, Builder> {

        private ObjectStreamClassPredicate predicate = new ObjectStreamClassPredicate();

        private boolean strict;

        private long maxDepth = -1;

        private long maxArrayLength = -1;

        private long maxRefs = -1;

        /**
         * Constructs a new builder of {@link ValidatingObjectInputStream}.
         *
         * @deprecated Use {@link #builder()}.
         */
        @Deprecated
        public Builder() {
            // empty
        }

        /**
         * Accepts the specified classes for deserialization, unless they are otherwise rejected.
         * <p>
         * The reject list takes precedence over the accept list.
         * </p>
         *
         * <p>
         * Classes are matched by their exact names. Array classes must be accepted separately from their element classes.
         * For example, accepting {@code Foo.class} does not accept {@code Foo[].class}.
         * </p>
         *
         * @param classes Classes to accept.
         * @return this object.
         * @since 2.18.0
         */
        public Builder accept(final Class<?>... classes) {
            predicate.accept(classes);
            return this;
        }

        /**
         * Accepts class names where the supplied ClassNameMatcher matches for deserialization, unless they are otherwise rejected.
         * <p>
         * The reject list takes precedence over the accept list.
         * </p>
         *
         * @param matcher A class name matcher to <em>accept</em> objects.
         * @return {@code this} instance.
         * @since 2.18.0
         */
        public Builder accept(final ClassNameMatcher matcher) {
            predicate.accept(matcher);
            return this;
        }

        /**
         * Accepts class names that match the supplied pattern for deserialization, unless they are otherwise rejected.
         * <p>
         * The reject list takes precedence over the accept list.
         * </p>
         *
         * <p>
         * The pattern must match the entire class name, as defined by {@link java.util.regex.Matcher#matches()}.
         * For example, {@code foo\.Bar} matches {@code foo.Bar}, but not {@code foo.BarExtra}. Use {@code .*foo\.Bar.*} to match a substring.
         * </p>
         *
         * @param pattern A Pattern for compiled regular expression.
         * @return {@code this} instance.
         * @since 2.18.0
         */
        public Builder accept(final Pattern pattern) {
            predicate.accept(pattern);
            return this;
        }

        /**
         * Accepts the wildcard specified classes for deserialization, unless they are otherwise rejected.
         * <p>
         * The reject list takes precedence over the accept list.
         * </p>
         *
         * <p>
         * Matching is case-sensitive. The wildcard {@code *} matches zero or more characters and {@code ?} matches one character, including {@code '.'} in both
         * cases. Unlike <a href="https://openjdk.org/jeps/290">JEP 290</a> serialization filter patterns, these patterns do not treat package boundaries
         * specially: {@code com.example.*} matches classes in {@code com.example} and all its subpackages, and {@code **} is equivalent to {@code *}.
         * </p>
         *
         * <p>
         * Use {@link #accept(Pattern)} with a regular expression to match only classes directly in a package.
         * </p>
         *
         * @param patterns Wildcard file name patterns as defined by {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String)
         *                 FilenameUtils.wildcardMatch}
         * @return {@code this} instance.
         * @since 2.18.0
         */
        public Builder accept(final String... patterns) {
            predicate.accept(patterns);
            return this;
        }

        /**
         * Builds a new {@link ValidatingObjectInputStream}.
         * <p>
         * The reject list takes precedence over the accept list.
         * </p>
         * <p>
         * You must set an aspect that supports {@link #getInputStream()} on this builder, otherwise, this method throws an exception.
         * </p>
         * <p>
         * This builder uses the following aspects:
         * </p>
         * <ul>
         * <li>{@link #getInputStream()} gets the target aspect.</li>
         * <li>predicate</li>
         * <li>charsetDecoder</li>
         * <li>writeImmediately</li>
         * </ul>
         *
         * @return A new instance.
         * @throws UnsupportedOperationException if the origin cannot provide a {@link InputStream}.
         * @throws IOException                   if an I/O error occurs converting to an {@link InputStream} using {@link #getInputStream()}.
         * @see #getWriter()
         * @see #getUnchecked()
         */
        @Override
        public ValidatingObjectInputStream get() throws IOException {
            return new ValidatingObjectInputStream(this);
        }

        /**
         * Gets the predicate.
         *
         * @return The predicate.
         * @since 2.18.0
         */
        public ObjectStreamClassPredicate getPredicate() {
            return predicate;
        }

        /**
         * Rejects the specified classes for deserialization, even if they are otherwise accepted.
         * <p>
         * The reject list takes precedence over the accept list.
         * </p>
         *
         * <p>
         * Classes are matched by their exact names. Rejecting {@code Foo.class} does not reject {@code Foo[].class}, so an otherwise accepted
         * empty or null-only array can still be deserialized. Element class descriptors encountered while reading array contents are checked separately.
         * To reject the array class itself, also reject {@code Foo[].class}.
         * </p>
         *
         * @param classes Classes to reject.
         * @return {@code this} instance.
         * @since 2.18.0
         */
        public Builder reject(final Class<?>... classes) {
            predicate.reject(classes);
            return this;
        }

        /**
         * Rejects class names where the supplied ClassNameMatcher matches for deserialization, even if they are otherwise accepted.
         * <p>
         * The reject list takes precedence over the accept list.
         * </p>
         *
         * @param matcher The matcher to use.
         * @return {@code this} instance.
         * @since 2.18.0
         */
        public Builder reject(final ClassNameMatcher matcher) {
            predicate.reject(matcher);
            return this;
        }

        /**
         * Rejects class names that match the supplied pattern for deserialization, even if they are otherwise accepted.
         * <p>
         * The reject list takes precedence over the accept list.
         * </p>
         *
         * <p>
         * The pattern must match the entire class name, as defined by {@link java.util.regex.Matcher#matches()}.
         * For example, {@code foo\.Bar} matches {@code foo.Bar}, but not {@code foo.BarExtra}. Use {@code .*foo\.Bar.*} to match a substring.
         * </p>
         *
         * @param pattern standard Java regexp.
         * @return {@code this} instance.
         * @since 2.18.0
         */
        public Builder reject(final Pattern pattern) {
            predicate.reject(pattern);
            return this;
        }

        /**
         * Rejects the wildcard specified classes for deserialization, even if they are otherwise accepted.
         * <p>
         * The reject list takes precedence over the accept list.
         * </p>
         *
         * <p>Matching is case-sensitive. The wildcard {@code *} matches zero or more characters and {@code ?} matches one character, including
         * {@code '.'} in both cases. Unlike <a href="https://openjdk.org/jeps/290">JEP 290</a> serialization filter patterns, these patterns do not
         * treat package boundaries specially: {@code com.example.*} matches classes in {@code com.example} and all its subpackages, and
         * {@code **} is equivalent to {@code *}.</p>
         *
         * <p>Use {@link #reject(Pattern)} with a regular expression to match only classes directly in a package.</p>
         *
         * @param patterns Wildcard file name patterns as defined by {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String)
         *                 FilenameUtils.wildcardMatch}
         * @return {@code this} instance.
         * @since 2.18.0
         */
        public Builder reject(final String... patterns) {
            predicate.reject(patterns);
            return this;
        }

        /**
         * Sets the maximum declared length accepted for any array in the stream, like the <a href="https://openjdk.org/jeps/290">JEP 290</a>
         * {@code maxarray} limit.
         * <p>
         * An array's length is declared in the stream and the array is allocated <em>before</em> its contents are read, so without this limit a stream of a
         * few dozen bytes can force a ~2 GiB allocation regardless of any {@link BoundedInputStream} byte bound. Enforced through the platform
         * {@code ObjectInputFilter} (Java 8u121 and up); building throws {@link UnsupportedOperationException} on older JVMs where it cannot be enforced.
         * </p>
         *
         * @param maxArrayLength The maximum declared array length, a negative value disables the limit (the default).
         * @return {@code this} instance.
         * @since 2.23.0
         */
        public Builder setMaxArrayLength(final long maxArrayLength) {
            this.maxArrayLength = maxArrayLength;
            return this;
        }

        /**
         * Sets the maximum nesting depth accepted for the object graph, like the <a href="https://openjdk.org/jeps/290">JEP 290</a> {@code maxdepth} limit.
         * <p>
         * Deserialization is recursive: deeply nested containers exhaust the call stack with a {@link StackOverflowError}, an {@link Error} that typical
         * {@link IOException} handling does not catch. Enforced through the platform {@code ObjectInputFilter} (Java 8u121 and up); building throws
         * {@link UnsupportedOperationException} on older JVMs where it cannot be enforced.
         * </p>
         *
         * @param maxDepth The maximum object graph depth, a negative value disables the limit (the default).
         * @return {@code this} instance.
         * @since 2.23.0
         */
        public Builder setMaxDepth(final long maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        /**
         * Sets the maximum number of internal object references accepted in the stream, like the <a href="https://openjdk.org/jeps/290">JEP 290</a>
         * {@code maxrefs} limit; together with {@link #setMaxArrayLength(long)} this also bounds {@code TC_REFERENCE} back-reference amplification.
         * <p>
         * Enforced through the platform {@code ObjectInputFilter} (Java 8u121 and up); building throws {@link UnsupportedOperationException} on older JVMs
         * where it cannot be enforced. This count includes back-references, rather than only distinct objects.
         * </p>
         *
         * @param maxRefs The maximum number of object references, a negative value disables the limit (the default).
         * @return {@code this} instance.
         * @since 2.23.0
         */
        public Builder setMaxRefs(final long maxRefs) {
            this.maxRefs = maxRefs;
            return this;
        }

        /**
         * Sets the predicate, null resets to an empty new ObjectStreamClassPredicate.
         * <p>
         * The reject list takes precedence over the accept list.
         * </p>
         *
         * @param predicate The predicate.
         * @return {@code this} instance.
         * @since 2.18.0
         */
        public Builder setPredicate(final ObjectStreamClassPredicate predicate) {
            this.predicate = predicate != null ? predicate : new ObjectStreamClassPredicate();
            return this;
        }

        /**
         * Sets whether to validate interfaces and runtime-visible annotations recursively.
         * <p>
         * For compatibility with previous versions, this is false by default.
         * </p>
         * <p>
         * Checks:
         * </p>
         * <ol>
         * <li>The type name.</li>
         * <li>The interfaces implemented by the class, recursively.</li>
         * <li>The runtime-visible annotations on this type, recursively.</li>
         * </ol>
         * <p>
         * The reject list takes precedence over the accept list.
         * </p>
         *
         * @param strict If true, checks that all interfaces and annotations for a given type are not rejected and accepted.
         * @return {@code this} instance.
         * @since 2.23.0
         */
        public Builder setStrict(final boolean strict) {
            this.strict = strict;
            return this;
        }

    }

    /**
     * Constructs a new {@link Builder}.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @return A new {@link Builder}.
     * @since 2.18.0
     */
    public static Builder builder() {
        return new Builder();
    }

    private static Object invokeFilter(final Method method, final Object filter, final Object[] args) throws Throwable {
        try {
            return method.invoke(filter, args);
        } catch (final InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Installs resource limits while preserving rejections from the stream's existing filter.
     *
     * @param stream the stream to configure.
     * @param pattern the resource limit pattern.
     * @param loader the loader used to discover the platform filter API.
     * @throws IOException if the filter API cannot be accessed.
     */
    static void setObjectInputFilter(final ObjectInputStream stream, final String pattern, final ClassLoader loader) throws IOException {
        final Class<?> filterClass;
        boolean legacy = false;
        Class<?> discovered;
        try {
            discovered = Class.forName("java.io.ObjectInputFilter", true, loader);
        } catch (final ClassNotFoundException absent) {
            try {
                discovered = Class.forName("sun.misc.ObjectInputFilter", true, loader);
                legacy = true;
            } catch (final ClassNotFoundException unavailable) {
                throw new UnsupportedOperationException("Resource limits require ObjectInputFilter (JEP 290, Java 8u121+)", unavailable);
            }
        }
        filterClass = discovered;
        try {
            final Class<?> configClass = Class.forName(filterClass.getName() + "$Config", true, loader);
            final Object limits = configClass.getMethod("createFilter", String.class).invoke(null, pattern);
            final Object current = legacy
                    ? configClass.getMethod("getObjectInputFilter", ObjectInputStream.class).invoke(null, stream)
                    : ObjectInputStream.class.getMethod("getObjectInputFilter").invoke(stream);
            final Object combined;
            if (current == null) {
                combined = limits;
            } else {
                final Class<?> infoClass = Class.forName(filterClass.getName() + "$FilterInfo", true, loader);
                final Method checkInput = filterClass.getMethod("checkInput", infoClass);
                combined = Proxy.newProxyInstance(filterClass.getClassLoader(), new Class<?>[] {filterClass}, (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        switch (method.getName()) {
                        case "equals":
                            return proxy == args[0];
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        default:
                            return "Resource limits combined with existing deserialization filter";
                        }
                    }
                    final Object existingStatus = invokeFilter(checkInput, current, args);
                    if (existingStatus == null || "REJECTED".equals(existingStatus.toString())) {
                        return existingStatus;
                    }
                    final Object limitStatus = invokeFilter(checkInput, limits, args);
                    return "UNDECIDED".equals(limitStatus.toString()) ? existingStatus : limitStatus;
                });
            }
            if (legacy) {
                configClass.getMethod("setObjectInputFilter", ObjectInputStream.class, filterClass).invoke(null, stream, combined);
            } else {
                ObjectInputStream.class.getMethod("setObjectInputFilter", filterClass).invoke(stream, combined);
            }
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IOException("Cannot install deserialization resource limits", cause);
        } catch (final ReflectiveOperationException e) {
            throw new IOException("Cannot access deserialization filter API", e);
        }
    }

    private final ObjectStreamClassPredicate predicate;
    private final boolean strict;

    /**
     * Constructs an instance to deserialize the specified input stream. At least one accept method needs to be called to specify which classes can be
     * deserialized, as by default no classes are accepted.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param builder     The builder.
     */
    @SuppressWarnings("resource") // caller closes
    private ValidatingObjectInputStream(final Builder builder) throws IOException {
        super(builder.getInputStream());
        this.predicate = builder.predicate;
        this.strict = builder.strict;
        final StringBuilder pattern = new StringBuilder();
        if (builder.maxDepth >= 0) {
            pattern.append("maxdepth=").append(builder.maxDepth).append(';');
        }
        if (builder.maxArrayLength >= 0) {
            pattern.append("maxarray=").append(builder.maxArrayLength).append(';');
        }
        if (builder.maxRefs >= 0) {
            pattern.append("maxrefs=").append(builder.maxRefs).append(';');
        }
        if (pattern.length() != 0) {
            setObjectInputFilter(this, pattern.toString(), ValidatingObjectInputStream.class.getClassLoader());
        }
    }

    /**
     * Constructs an instance to deserialize the specified input stream. At least one accept method needs to be called to specify which classes can be
     * deserialized, as by default no classes are accepted.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param input An input stream.
     * @throws IOException Thrown if an I/O error occurs while reading stream header.
     * @deprecated Use {@link #builder()}.
     */
    @Deprecated
    public ValidatingObjectInputStream(final InputStream input) throws IOException {
        this(builder().setInputStream(input).setPredicate(new ObjectStreamClassPredicate()));
    }

    /**
     * Accepts the specified classes for deserialization, unless they are otherwise rejected.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * <p>
     * Classes are matched by their exact names. Array classes must be accepted separately from their element classes.
     * For example, accepting {@code Foo.class} does not accept {@code Foo[].class}.
     * </p>
     *
     * @param classes Classes to accept.
     * @return {@code this} instance.
     */
    public ValidatingObjectInputStream accept(final Class<?>... classes) {
        predicate.accept(classes);
        return this;
    }

    /**
     * Accepts class names where the supplied ClassNameMatcher matches for deserialization, unless they are otherwise rejected.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param matcher A class name matcher to <em>accept</em> objects.
     * @return {@code this} instance.
     */
    public ValidatingObjectInputStream accept(final ClassNameMatcher matcher) {
        predicate.accept(matcher);
        return this;
    }

    /**
     * Accepts class names that match the supplied pattern for deserialization, unless they are otherwise rejected.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * <p>
     * The pattern must match the entire class name, as defined by {@link java.util.regex.Matcher#matches()}.
     * For example, {@code foo\.Bar} matches {@code foo.Bar}, but not {@code foo.BarExtra}. Use {@code .*foo\.Bar.*} to match a substring.
     * </p>
     *
     * @param pattern A Pattern for compiled regular expression.
     * @return {@code this} instance.
     */
    public ValidatingObjectInputStream accept(final Pattern pattern) {
        predicate.accept(pattern);
        return this;
    }

    /**
     * Accepts the wildcard specified classes for deserialization, unless they are otherwise rejected.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * <p>
     * Matching is case-sensitive. The wildcard {@code *} matches zero or more characters and {@code ?} matches one character, including {@code '.'} in both
     * cases. Unlike <a href="https://openjdk.org/jeps/290">JEP 290</a> serialization filter patterns, these patterns do not treat package boundaries specially:
     * {@code com.example.*} matches classes in {@code com.example} and all its subpackages, and {@code **} is equivalent to {@code *}.
     * </p>
     *
     * <p>
     * Use {@link #accept(Pattern)} with a regular expression to match only classes directly in a package.
     * </p>
     *
     * @param patterns Wildcard file name patterns as defined by {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String)
     *                 FilenameUtils.wildcardMatch}.
     * @return {@code this} instance.
     */
    public ValidatingObjectInputStream accept(final String... patterns) {
        predicate.accept(patterns);
        return this;
    }

    /**
     * Checks that the given type can be legally resolved as configured.
     * <p>
     * Checks the type name, its interfaces, and its runtime-visible annotations recursively. Each type is visited once per traversal to avoid
     * annotation cycles.
     * </p>
     *
     * @param result The type to check.
     * @throws InvalidClassException Thrown when a rejected or non-accepted type is found.
     */
    private void check(final Class<?> result) throws InvalidClassException {
        check(result, new HashSet<>());
    }

    /**
     * Checks a type and recursively checks its interfaces and runtime-visible annotations.
     *
     * @param result The type to check.
     * @param visited The types already visited in this traversal.
     * @throws InvalidClassException Thrown when a rejected or non-accepted type is found.
     */
    private void check(final Class<?> result, final Set<Class<?>> visited) throws InvalidClassException {
        if (!visited.add(result)) {
            return;
        }
        checkTypeName(result.getName());
        for (final Class<?> iface : result.getInterfaces()) {
            check(iface, visited);
        }
        for (final Annotation annotation : result.getAnnotations()) {
            check(annotation.annotationType(), visited);
        }
    }

    /**
     * Checks that the type name conforms to requirements.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param name The type name to test.
     * @throws InvalidClassException Thrown when a rejected or non-accepted class is found.
     */
    private void checkTypeName(final String name) throws InvalidClassException {
        if (!predicate.test(name)) {
            invalidClassNameFound(name);
        }
    }

    /**
     * Called to throw {@link InvalidClassException} if an invalid class name is found during deserialization. Can be overridden, for example to log those class
     * names.
     *
     * @param className name of the invalid class.
     * @throws InvalidClassException Thrown with a message containing the class name.
     */
    protected void invalidClassNameFound(final String className) throws InvalidClassException {
        throw new InvalidClassException("Class name not accepted: " + className);
    }

    /**
     * Delegates to {@link #readObject()} and casts to the generic {@code T}.
     *
     * @param <T> The return type.
     * @return Result from {@link #readObject()}.
     * @throws ClassNotFoundException Thrown by {@link #readObject()}.
     * @throws IOException            Thrown by {@link #readObject()}.
     * @throws ClassCastException     Thrown when {@link #readObject()} does not match {@code T}.
     * @since 2.18.0
     */
    @SuppressWarnings("unchecked")
    public <T> T readObjectCast() throws ClassNotFoundException, IOException {
        return (T) super.readObject();
    }

    /**
     * Rejects the specified classes for deserialization, even if they are otherwise accepted.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * <p>
     * Classes are matched by their exact names. Rejecting {@code Foo.class} does not reject {@code Foo[].class}, so an otherwise accepted
     * empty or null-only array can still be deserialized. Element class descriptors encountered while reading array contents are checked separately.
     * To reject the array class itself, also reject {@code Foo[].class}.
     * </p>
     *
     * @param classes Classes to reject.
     * @return {@code this} instance.
     */
    public ValidatingObjectInputStream reject(final Class<?>... classes) {
        predicate.reject(classes);
        return this;
    }

    /**
     * Rejects class names where the supplied ClassNameMatcher matches for deserialization, even if they are otherwise accepted.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param matcher A class name matcher to <em>reject</em> objects.
     * @return {@code this} instance.
     */
    public ValidatingObjectInputStream reject(final ClassNameMatcher matcher) {
        predicate.reject(matcher);
        return this;
    }

    /**
     * Rejects class names that match the supplied pattern for deserialization, even if they are otherwise accepted.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * <p>
     * The pattern must match the entire class name, as defined by {@link java.util.regex.Matcher#matches()}.
     * For example, {@code foo\.Bar} matches {@code foo.Bar}, but not {@code foo.BarExtra}. Use {@code .*foo\.Bar.*} to match a substring.
     * </p>
     *
     * @param pattern A Pattern for compiled regular expression.
     * @return {@code this} instance.
     */
    public ValidatingObjectInputStream reject(final Pattern pattern) {
        predicate.reject(pattern);
        return this;
    }

    /**
     * Rejects the wildcard specified classes for deserialization, even if they are otherwise accepted.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * <p>
     * Matching is case-sensitive. The wildcard {@code *} matches zero or more characters and {@code ?} matches one character, including {@code '.'} in both
     * cases. Unlike <a href="https://openjdk.org/jeps/290">JEP 290</a> serialization filter patterns, these patterns do not treat package boundaries specially:
     * {@code com.example.*} matches classes in {@code com.example} and all its subpackages, and {@code **} is equivalent to {@code *}.
     * </p>
     *
     * <p>
     * Use {@link #reject(Pattern)} with a regular expression to match only classes directly in a package.
     * </p>
     *
     * @param patterns An array of wildcard file name patterns as defined by {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String)
     *                 FilenameUtils.wildcardMatch}
     * @return {@code this} instance.
     */
    public ValidatingObjectInputStream reject(final String... patterns) {
        predicate.reject(patterns);
        return this;
    }

    /**
     * Checks that the given object's class name conforms to requirements and if so delegates to the superclass.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     */
    @Override
    protected Class<?> resolveClass(final ObjectStreamClass osc) throws IOException, ClassNotFoundException {
        checkTypeName(osc.getName());
        // resolveClass() calls Class.forName(String, boolean, ClassLoader) with initialize set to false.
        // The result Class is therefore not initialized.
        final Class<?> result = super.resolveClass(osc);
        if (strict) {
            check(result);
        }
        return result;
    }

    /**
     * Checks that the given names conform to requirements and if so delegates to the superclass.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     * <p>
     * In strict mode, each interface of the resolved proxy class is checked recursively, including its super-interfaces and runtime-visible annotations,
     * as described by {@link Builder#setStrict(boolean)}. The generated proxy class name is not checked. Without strict mode, only the interface names
     * supplied by the stream are checked.
     * </p>
     */
    @Override
    protected Class<?> resolveProxyClass(final String[] interfaces) throws IOException, ClassNotFoundException {
        for (final String interfaceName : interfaces) {
            checkTypeName(interfaceName);
        }
        final Class<?> result = super.resolveProxyClass(interfaces);
        if (strict) {
            // The proxy class itself has a synthetic name; validate its interfaces recursively,
            // exactly like resolveClass() validates a resolved class in strict mode.
            final Set<Class<?>> visited = new HashSet<>();
            for (final Class<?> iface : result.getInterfaces()) {
                check(iface, visited);
            }
        }
        return result;
    }
}
