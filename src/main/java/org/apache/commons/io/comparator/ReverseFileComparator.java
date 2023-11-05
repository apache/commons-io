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
import java.util.Objects;

/**
 * Reverses the result of comparing two {@link File} objects using the delegate {@link Comparator}.
 * <h2>Deprecating Serialization</h2>
 * <p>
 * <em>Serialization is deprecated and will be removed in 3.0.</em>
 * </p>
 *
 * @since 1.4
 */
final class ReverseFileComparator extends AbstractFileComparator implements Serializable {

    private static final long serialVersionUID = -4808255005272229056L;
    private final Comparator<File> delegate;

    /**
     * Constructs an instance with the specified delegate {@link Comparator}.
     *
     * @param delegate The comparator to delegate to.
     */
    public ReverseFileComparator(final Comparator<File> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * Compares using the delegate Comparator, reversing the result.
     *
     * @param file1 The first file to compare.
     * @param file2 The second file to compare.
     * @return the result from the delegate {@link Comparator#compare(Object, Object)} reversing the value (i.e.
     *         positive becomes negative and vice versa).
     */
    @Override
    public int compare(final File file1, final File file2) {
        return delegate.compare(file2, file1); // parameters switched round
    }

    /**
     * Returns the String representation of this file comparator.
     *
     * @return String representation of this file comparator.
     */
    @Override
    public String toString() {
        return super.toString() + "[" + delegate.toString() + "]";
    }

}
