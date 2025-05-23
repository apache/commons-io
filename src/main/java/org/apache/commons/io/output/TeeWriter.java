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

package org.apache.commons.io.output;

import java.io.Writer;
import java.util.Collection;

/**
 * Classic splitter of {@link Writer}. Named after the Unix 'tee' command. It allows a stream to be branched off so
 * there are now two streams.
 * <p>
 * This currently a only convenience class with the proper name "TeeWriter".
 * </p>
 *
 * @since 2.7
 */
public class TeeWriter extends ProxyCollectionWriter {

    /**
     * Constructs a new filtered collection writer.
     *
     * @param writers Writers to provide the underlying targets.
     */
    public TeeWriter(final Collection<Writer> writers) {
        super(writers);
    }

    /**
     * Constructs a new filtered collection writer.
     *
     * @param writers Writers to provide the underlying targets.
     */
    public TeeWriter(final Writer... writers) {
        super(writers);
    }
}
