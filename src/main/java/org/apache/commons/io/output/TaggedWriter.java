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
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.UUID;

import org.apache.commons.io.TaggedIOException;

/**
 * A writer decorator that tags potential exceptions so that the
 * reader that caused the exception can easily be identified. This is
 * done by using the {@link TaggedIOException} class to wrap all thrown
 * {@link IOException}s. See below for an example of using this class.
 * <pre>
 * TaggedReader reader = new TaggedReader(...);
 * try {
 *     // Processing that may throw an IOException either from this reader
 *     // or from some other IO activity like temporary files, etc.
 *     writeToWriter(writer);
 * } catch (IOException e) {
 *     if (writer.isCauseOf(e)) {
 *         // The exception was caused by this writer.
 *         // Use e.getCause() to get the original exception.
 *     } else {
 *         // The exception was caused by something else.
 *     }
 * }
 * </pre>
 * <p>
 * Alternatively, the {@link #throwIfCauseOf(Exception)} method can be
 * used to let higher levels of code handle the exception caused by this
 * writer while other processing errors are being taken care of at this
 * lower level.
 * </p>
 * <pre>
 * TaggedWriter writer = new TaggedWriter(...);
 * try {
 *     writeToWriter(writer);
 * } catch (IOException e) {
 *     writer.throwIfCauseOf(e);
 *     // ... or process the exception that was caused by something else
 * }
 * </pre>
 *
 * @see TaggedIOException
 * @since 2.0
 */
public class TaggedWriter extends ProxyWriter {

    /**
     * The unique tag associated with exceptions from writer.
     */
    private final Serializable tag = UUID.randomUUID();

    /**
     * Constructs a tagging decorator for the given writer.
     *
     * @param proxy writer to be decorated
     */
    public TaggedWriter(final Writer proxy) {
        super(proxy);
    }

    /**
     * Tags any IOExceptions thrown, wrapping and re-throwing.
     *
     * @param e The IOException thrown
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected void handleIOException(final IOException e) throws IOException {
        throw new TaggedIOException(e, tag);
    }

    /**
     * Tests if the given exception was caused by this writer.
     *
     * @param exception an exception
     * @return {@code true} if the exception was thrown by this writer,
     *         {@code false} otherwise
     */
    public boolean isCauseOf(final Exception exception) {
        return TaggedIOException.isTaggedWith(exception, tag);
    }

    /**
     * Re-throws the original exception thrown by this writer. This method
     * first checks whether the given exception is a {@link TaggedIOException}
     * wrapper created by this decorator, and then unwraps and throws the
     * original wrapped exception. Returns normally if the exception was
     * not thrown by this writer.
     *
     * @param exception an exception
     * @throws IOException original exception, if any, thrown by this writer
     */
    public void throwIfCauseOf(final Exception exception) throws IOException {
        TaggedIOException.throwCauseIfTaggedWith(exception, tag);
    }

}
