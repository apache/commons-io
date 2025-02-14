/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.regex.Pattern;

import org.apache.commons.io.build.AbstractStreamBuilder;

/**
 * An {@link ObjectInputStream} that's restricted to deserialize a limited set of classes.
 *
 * <p>
 * Various accept/reject methods allow for specifying which classes can be deserialized.
 * </p>
 * <h2>Reading safely</h2>
 * <p>
 * Here is the only way to safely read a HashMap of String keys and Integer values:
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
 * // Reading
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
 * Design inspired by a <a href="http://www.ibm.com/developerworks/library/se-lookahead/">IBM DeveloperWorks Article</a>.
 * </p>
 *
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
         *
         * @param classes Classes to accept
         * @return this object
         * @since 2.18.0
         */
        public Builder accept(final Class<?>... classes) {
            predicate.accept(classes);
            return this;
        }

        /**
         * Accepts class names where the supplied ClassNameMatcher matches for deserialization, unless they are otherwise rejected.
         *
         * @param matcher a class name matcher to <em>accept</em> objects.
         * @return this instance.
         * @since 2.18.0
         */
        public Builder accept(final ClassNameMatcher matcher) {
            predicate.accept(matcher);
            return this;
        }

        /**
         * Accepts class names that match the supplied pattern for deserialization, unless they are otherwise rejected.
         *
         * @param pattern a Pattern for compiled regular expression.
         * @return this instance.
         * @since 2.18.0
         */
        public Builder accept(final Pattern pattern) {
            predicate.accept(pattern);
            return this;
        }

        /**
         * Accepts the wildcard specified classes for deserialization, unless they are otherwise rejected.
         *
         * @param patterns Wildcard file name patterns as defined by {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String)
         *                 FilenameUtils.wildcardMatch}
         * @return this instance.
         * @since 2.18.0
         */
        public Builder accept(final String... patterns) {
            predicate.accept(patterns);
            return this;
        }

        /**
         * Builds a new {@link ValidatingObjectInputStream}.
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
         * @return a new instance.
         * @throws UnsupportedOperationException if the origin cannot provide a {@link InputStream}.
         * @throws IOException                   if an I/O error occurs converting to an {@link InputStream} using {@link #getInputStream()}.
         * @see #getWriter()
         * @see #getUnchecked()
         */
        @Override
        public ValidatingObjectInputStream get() throws IOException {
            return new ValidatingObjectInputStream(getInputStream(), predicate);
        }

        /**
         * Gets the predicate.
         *
         * @return the predicate.
         * @since 2.18.0
         */
        public ObjectStreamClassPredicate getPredicate() {
            return predicate;
        }

        /**
         * Rejects the specified classes for deserialization, even if they are otherwise accepted.
         *
         * @param classes Classes to reject
         * @return this instance.
         * @since 2.18.0
         */
        public Builder reject(final Class<?>... classes) {
            predicate.reject(classes);
            return this;
        }

        /**
         * Rejects class names where the supplied ClassNameMatcher matches for deserialization, even if they are otherwise accepted.
         *
         * @param matcher the matcher to use
         * @return this instance.
         * @since 2.18.0
         */
        public Builder reject(final ClassNameMatcher matcher) {
            predicate.reject(matcher);
            return this;
        }

        /**
         * Rejects class names that match the supplied pattern for deserialization, even if they are otherwise accepted.
         *
         * @param pattern standard Java regexp
         * @return this instance.
         * @since 2.18.0
         */
        public Builder reject(final Pattern pattern) {
            predicate.reject(pattern);
            return this;
        }

        /**
         * Rejects the wildcard specified classes for deserialization, even if they are otherwise accepted.
         *
         * @param patterns Wildcard file name patterns as defined by {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String)
         *                 FilenameUtils.wildcardMatch}
         * @return this instance.
         * @since 2.18.0
         */
        public Builder reject(final String... patterns) {
            predicate.reject(patterns);
            return this;
        }

        /**
         * Sets the predicate, null resets to an empty new ObjectStreamClassPredicate.
         *
         * @param predicate the predicate.
         * @return this instance.
         * @since 2.18.0
         */
        public Builder setPredicate(final ObjectStreamClassPredicate predicate) {
            this.predicate = predicate != null ? predicate : new ObjectStreamClassPredicate();
            return this;
        }

    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     * @since 2.18.0
     */
    public static Builder builder() {
        return new Builder();
    }

    private final ObjectStreamClassPredicate predicate;

    /**
     * Constructs an instance to deserialize the specified input stream. At least one accept method needs to be called to specify which classes can be
     * deserialized, as by default no classes are accepted.
     *
     * @param input an input stream
     * @throws IOException if an I/O error occurs while reading stream header
     * @deprecated Use {@link #builder()}.
     */
    @Deprecated
    public ValidatingObjectInputStream(final InputStream input) throws IOException {
        this(input, new ObjectStreamClassPredicate());
    }

    /**
     * Constructs an instance to deserialize the specified input stream. At least one accept method needs to be called to specify which classes can be
     * deserialized, as by default no classes are accepted.
     *
     * @param input     an input stream.
     * @param predicate how to accept and reject classes.
     * @throws IOException if an I/O error occurs while reading stream header.
     */
    private ValidatingObjectInputStream(final InputStream input, final ObjectStreamClassPredicate predicate) throws IOException {
        super(input);
        this.predicate = predicate;
    }

    /**
     * Accepts the specified classes for deserialization, unless they are otherwise rejected.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param classes Classes to accept
     * @return this instance.
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
     * @param matcher a class name matcher to <em>accept</em> objects.
     * @return this instance.
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
     * @param pattern a Pattern for compiled regular expression.
     * @return this instance.
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
     * @param patterns Wildcard file name patterns as defined by {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String)
     *                 FilenameUtils.wildcardMatch}.
     * @return this instance.
     */
    public ValidatingObjectInputStream accept(final String... patterns) {
        predicate.accept(patterns);
        return this;
    }

    /**
     * Checks that the class name conforms to requirements.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param name The class name to test.
     * @throws InvalidClassException Thrown when a rejected or non-accepted class is found.
     */
    private void checkClassName(final String name) throws InvalidClassException {
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
     * @param classes Classes to reject.
     * @return this instance.
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
     * @param matcher a class name matcher to <em>reject</em> objects.
     * @return this instance.
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
     * @param pattern a Pattern for compiled regular expression.
     * @return this instance.
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
     * @param patterns An array of wildcard file name patterns as defined by {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String)
     *                 FilenameUtils.wildcardMatch}
     * @return this instance.
     */
    public ValidatingObjectInputStream reject(final String... patterns) {
        predicate.reject(patterns);
        return this;
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass osc) throws IOException, ClassNotFoundException {
        checkClassName(osc.getName());
        return super.resolveClass(osc);
    }
}
