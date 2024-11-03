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

import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A predicate (boolean-valued function) of one argument to accept and reject classes.
 * <p>
 * The reject list takes precedence over the accept list.
 * </p>
 *
 * @since 2.18.0
 */
public class ObjectStreamClassPredicate implements Predicate<ObjectStreamClass> {

    // This is not a Set for now to avoid ClassNameMatchers requiring proper implementations of hashCode() and equals().
    private final List<ClassNameMatcher> acceptMatchers = new ArrayList<>();

    // This is not a Set for now to avoid ClassNameMatchers requiring proper implementations of hashCode() and equals().
    private final List<ClassNameMatcher> rejectMatchers = new ArrayList<>();

    /**
     * Constructs a new instance.
     */
    public ObjectStreamClassPredicate() {
        // empty
    }

    /**
     * Accepts the specified classes for deserialization, unless they are otherwise rejected.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param classes Classes to accept
     * @return this object
     */
    public ObjectStreamClassPredicate accept(final Class<?>... classes) {
        Stream.of(classes).map(c -> new FullClassNameMatcher(c.getName())).forEach(acceptMatchers::add);
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
    public ObjectStreamClassPredicate accept(final ClassNameMatcher matcher) {
        acceptMatchers.add(matcher);
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
    public ObjectStreamClassPredicate accept(final Pattern pattern) {
        acceptMatchers.add(new RegexpClassNameMatcher(pattern));
        return this;
    }

    /**
     * Accepts the wildcard specified classes for deserialization, unless they are otherwise rejected.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param patterns Wildcard file name patterns as defined by {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String)
     *                 FilenameUtils.wildcardMatch}
     * @return this instance.
     */
    public ObjectStreamClassPredicate accept(final String... patterns) {
        Stream.of(patterns).map(WildcardClassNameMatcher::new).forEach(acceptMatchers::add);
        return this;
    }

    /**
     * Rejects the specified classes for deserialization, even if they are otherwise accepted.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param classes Classes to reject
     * @return this instance.
     */
    public ObjectStreamClassPredicate reject(final Class<?>... classes) {
        Stream.of(classes).map(c -> new FullClassNameMatcher(c.getName())).forEach(rejectMatchers::add);
        return this;
    }

    /**
     * Rejects class names where the supplied ClassNameMatcher matches for deserialization, even if they are otherwise accepted.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param m the matcher to use
     * @return this instance.
     */
    public ObjectStreamClassPredicate reject(final ClassNameMatcher m) {
        rejectMatchers.add(m);
        return this;
    }

    /**
     * Rejects class names that match the supplied pattern for deserialization, even if they are otherwise accepted.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param pattern standard Java regexp
     * @return this instance.
     */
    public ObjectStreamClassPredicate reject(final Pattern pattern) {
        rejectMatchers.add(new RegexpClassNameMatcher(pattern));
        return this;
    }

    /**
     * Rejects the wildcard specified classes for deserialization, even if they are otherwise accepted.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param patterns Wildcard file name patterns as defined by {@link org.apache.commons.io.FilenameUtils#wildcardMatch(String, String)
     *                 FilenameUtils.wildcardMatch}
     * @return this instance.
     */
    public ObjectStreamClassPredicate reject(final String... patterns) {
        Stream.of(patterns).map(WildcardClassNameMatcher::new).forEach(rejectMatchers::add);
        return this;
    }

    /**
     * Tests that the ObjectStreamClass conforms to requirements.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param objectStreamClass The ObjectStreamClass to test.
     * @return true if the input is accepted, false if rejected, false if neither.
     */
    @Override
    public boolean test(final ObjectStreamClass objectStreamClass) {
        return test(objectStreamClass.getName());
    }

    /**
     * Tests that the class name conforms to requirements.
     * <p>
     * The reject list takes precedence over the accept list.
     * </p>
     *
     * @param name The class name to test.
     * @return true if the input is accepted, false if rejected, false if neither.
     */
    public boolean test(final String name) {
        // The reject list takes precedence over the accept list.
        for (final ClassNameMatcher m : rejectMatchers) {
            if (m.matches(name)) {
                return false;
            }
        }
        for (final ClassNameMatcher m : acceptMatchers) {
            if (m.matches(name)) {
                return true;
            }
        }
        return false;
    }

}