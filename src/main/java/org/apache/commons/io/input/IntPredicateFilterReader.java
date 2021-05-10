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
 * A filter reader that removes characters represented as {@code int} code points that match a predicate,
 * handy to remove white space characters for example. This class is the most efficient way to filter out
 * multiple characters using a simple predicate.
 *
 * @since 2.9.0
 */
public class IntPredicateFilterReader extends AbstractCharacterFilterReader {

    private static final IntPredicate SKIP_NONE = ch -> false;

    private final IntPredicate skip;

    /**
     * Constructs a new reader.
     *
     * @param reader the reader to filter.
     * @param skip the predicates that determines which characters are filtered out.
     */
    public IntPredicateFilterReader(final Reader reader, final IntPredicate skip) {
        super(reader);
        this.skip = skip == null ? SKIP_NONE : skip;
    }

    @Override
    protected boolean filter(final int ch) {
        return skip.test(ch);
    }

}
