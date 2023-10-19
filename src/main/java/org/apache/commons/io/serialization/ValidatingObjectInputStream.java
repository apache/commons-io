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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * An {@link ObjectInputStream} that's restricted to deserialize
 * a limited set of classes.
 *
 * <p>
 * Various accept/reject methods allow for specifying which classes
 * can be deserialized.
 * </p>
 *
 * <p>
 * Design inspired by <a
 * href="http://www.ibm.com/developerworks/library/se-lookahead/">IBM
 * DeveloperWorks Article</a>.
 * </p>
 */
public class ValidatingObjectInputStream extends ObjectInputStream {
    private final List<ClassNameMatcher> acceptMatchers = new ArrayList<>();
    private final List<ClassNameMatcher> rejectMatchers = new ArrayList<>();

    /**
     * Constructs an object to deserialize the specified input stream.
     * At least one accept method needs to be called to specify which
     * classes can be deserialized, as by default no classes are
     * accepted.
     *
     * @param input an input stream
     * @throws IOException if an I/O error occurs while reading stream header
     */
    public ValidatingObjectInputStream(final InputStream input) throws IOException {
        super(input);
    }

    /**
     * Accept the specified classes for deserialization, unless they
     * are otherwise rejected.
     *
     * @param classes Classes to accept
     * @return this object
     */
    public ValidatingObjectInputStream accept(final Class<?>... classes) {
        Stream.of(classes).map(c -> new FullClassNameMatcher(c.getName())).forEach(acceptMatchers::add);
        return this;
    }

    /**
     * Accept class names where the supplied ClassNameMatcher matches for
     * deserialization, unless they are otherwise rejected.
     *
     * @param m the matcher to use
     * @return this object
     */
    public ValidatingObjectInputStream accept(final ClassNameMatcher m) {
        acceptMatchers.add(m);
        return this;
    }

    /**
     * Accept class names that match the supplied pattern for
     * deserialization, unless they are otherwise rejected.
     *
     * @param pattern standard Java regexp
     * @return this object
     */
    public ValidatingObjectInputStream accept(final Pattern pattern) {
        acceptMatchers.add(new RegexpClassNameMatcher(pattern));
        return this;
    }

    /**
     * Accept the wildcard specified classes for deserialization,
     * unless they are otherwise rejected.
     *
     * @param patterns Wildcard file name patterns as defined by
     *                  {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String) FilenameUtils.wildcardMatch}
     * @return this object
     */
    public ValidatingObjectInputStream accept(final String... patterns) {
        Stream.of(patterns).map(WildcardClassNameMatcher::new).forEach(acceptMatchers::add);
        return this;
    }

    /**
     * Checks that the class name conforms to requirements.
     *
     * @param name The class name
     * @throws InvalidClassException when a non-accepted class is encountered
     */
    private void checkClassName(final String name) throws InvalidClassException {
        // Reject has precedence over accept
        for (final ClassNameMatcher m : rejectMatchers) {
            if (m.matches(name)) {
                invalidClassNameFound(name);
            }
        }

        boolean ok = false;
        for (final ClassNameMatcher m : acceptMatchers) {
            if (m.matches(name)) {
                ok = true;
                break;
            }
        }
        if (!ok) {
            invalidClassNameFound(name);
        }
    }

    /**
     * Called to throw {@link InvalidClassException} if an invalid
     * class name is found during deserialization. Can be overridden, for example
     * to log those class names.
     *
     * @param className name of the invalid class
     * @throws InvalidClassException if the specified class is not allowed
     */
    protected void invalidClassNameFound(final String className) throws InvalidClassException {
        throw new InvalidClassException("Class name not accepted: " + className);
    }

    /**
     * Reject the specified classes for deserialization, even if they
     * are otherwise accepted.
     *
     * @param classes Classes to reject
     * @return this object
     */
    public ValidatingObjectInputStream reject(final Class<?>... classes) {
        Stream.of(classes).map(c -> new FullClassNameMatcher(c.getName())).forEach(rejectMatchers::add);
        return this;
    }

    /**
     * Reject class names where the supplied ClassNameMatcher matches for
     * deserialization, even if they are otherwise accepted.
     *
     * @param m the matcher to use
     * @return this object
     */
    public ValidatingObjectInputStream reject(final ClassNameMatcher m) {
        rejectMatchers.add(m);
        return this;
    }

    /**
     * Reject class names that match the supplied pattern for
     * deserialization, even if they are otherwise accepted.
     *
     * @param pattern standard Java regexp
     * @return this object
     */
    public ValidatingObjectInputStream reject(final Pattern pattern) {
        rejectMatchers.add(new RegexpClassNameMatcher(pattern));
        return this;
    }

    /**
     * Reject the wildcard specified classes for deserialization,
     * even if they are otherwise accepted.
     *
     * @param patterns Wildcard file name patterns as defined by
     *                  {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String) FilenameUtils.wildcardMatch}
     * @return this object
     */
    public ValidatingObjectInputStream reject(final String... patterns) {
        Stream.of(patterns).map(WildcardClassNameMatcher::new).forEach(rejectMatchers::add);
        return this;
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass osc) throws IOException, ClassNotFoundException {
        checkClassName(osc.getName());
        return super.resolveClass(osc);
    }
}