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
import java.util.function.IntPredicate;

/**
 * A filter reader that filters out a given character represented as an {@code int} code point, handy to remove
 * known junk characters from CSV files for example. This class is the most efficient way to filter out a single
 * character, as opposed to using a {@link CharacterSetFilterReader}. You can also nest {@link CharacterFilterReader}s.
 */
public class CharacterFilterReader extends AbstractCharacterFilterReader {

    /**
     * Constructs a new reader.
     *
     * @param reader
     *            the reader to filter.
     * @param skip
     *            the character to filter out.
     */
    public CharacterFilterReader(final Reader reader, final int skip) {
        super(reader, c -> c == skip);
    }

    /**
     * Constructs a new reader.
     *
     * @param reader the reader to filter.
     * @param skip Skip test.
     * @since 2.9.0
     */
    public CharacterFilterReader(final Reader reader, final IntPredicate skip) {
        super(reader, skip);
    }

}
