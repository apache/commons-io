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
package org.apache.commons.io.input;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntPredicate;

/**
 * A filter reader that removes a given set of characters represented as {@code int} code points, handy to remove known
 * junk characters from CSV files for example.
 * <p>
 * This class must convert each {@code int} read to an {@link Integer}. You can increase the Integer cache with a system
 * property, see {@link Integer}.
 * </p>
 */
public class CharacterSetFilterReader extends AbstractCharacterFilterReader {

    private static IntPredicate toIntPredicate(final Set<Integer> skip) {
        if (skip == null) {
            return SKIP_NONE;
        }
        final Set<Integer> unmodifiableSet = Collections.unmodifiableSet(skip);
        return c -> unmodifiableSet.contains(Integer.valueOf(c));
    }

    /**
     * Constructs a new reader.
     *
     * @param reader the reader to filter.
     * @param skip the set of characters to filter out.
     * @since 2.9.0
     */
    public CharacterSetFilterReader(final Reader reader, final Integer... skip) {
        this(reader, new HashSet<>(Arrays.asList(skip)));
    }

    /**
     * Constructs a new reader.
     *
     * @param reader the reader to filter.
     * @param skip the set of characters to filter out.
     */
    public CharacterSetFilterReader(final Reader reader, final Set<Integer> skip) {
        super(reader, toIntPredicate(skip));
    }

}
