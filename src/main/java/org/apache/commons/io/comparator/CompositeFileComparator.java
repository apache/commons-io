/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Compare two files using a set of delegate file {@link Comparator}.
 * <p>
 * This comparator can be used to sort lists or arrays of files by combining a number of other comparators.
 * <p>
 * Example of sorting a list of files by type (i.e. directory or file) and then by name:
 *
 * <pre>
 *       CompositeFileComparator comparator = new CompositeFileComparator(
 *           DirectoryFileComparator.DIRECTORY_COMPARATOR,
 *           NameFileComparator.NAME_COMPARATOR);
 *       List&lt;File&gt; list = ...
 *       comparator.sort(list);
 * </pre>
 * <h2>Deprecating Serialization</h2>
 * <p>
 * <em>Serialization is deprecated and will be removed in 3.0.</em>
 * </p>
 *
 * @since 2.0
 */
public class CompositeFileComparator extends AbstractFileComparator implements Serializable {

    private static final Comparator<?>[] EMPTY_COMPARATOR_ARRAY = {};
    private static final long serialVersionUID = -2224170307287243428L;

    /**
     * Delegates.
     */
    private final Comparator<File>[] delegates;

    /**
     * Constructs a composite comparator for the set of delegate comparators.
     *
     * @param delegates The delegate file comparators
     */
    public CompositeFileComparator(@SuppressWarnings("unchecked") final Comparator<File>... delegates) {
        this.delegates = delegates == null ? emptyArray() : delegates.clone();
    }

    /**
     * Constructs a composite comparator for the set of delegate comparators.
     *
     * @param delegates The delegate file comparators
     */
    public CompositeFileComparator(final Iterable<Comparator<File>> delegates) {
        this.delegates = delegates == null ? emptyArray()
                : StreamSupport.stream(delegates.spliterator(), false).toArray((IntFunction<Comparator<File>[]>) Comparator[]::new);
    }

    /**
     * Compares the two files using delegate comparators.
     *
     * @param file1 The first file to compare
     * @param file2 The second file to compare
     * @return the first non-zero result returned from the delegate comparators or zero.
     */
    @Override
    public int compare(final File file1, final File file2) {
        return Stream.of(delegates).map(delegate -> delegate.compare(file1, file2)).filter(r -> r != 0).findFirst().orElse(0);
    }

    @SuppressWarnings("unchecked") // types are already correct
    private Comparator<File>[] emptyArray() {
        return (Comparator<File>[]) EMPTY_COMPARATOR_ARRAY;
    }

    /**
     * String representation of this file comparator.
     *
     * @return String representation of this file comparator
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append('{');
        for (int i = 0; i < delegates.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(delegates[i]);
        }
        builder.append('}');
        return builder.toString();
    }
}
