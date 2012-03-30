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
package org.apache.commons.io;

import java.io.IOException;
import java.io.Serializable;

/**
 * An {@link IOException} decorator that adds a serializable tag to the
 * wrapped exception. Both the tag and the original exception can be used
 * to determine further processing when this exception is caught.
 *
 * @since 2.0
 */
public class TaggedIOException extends IOExceptionWithCause {

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -6994123481142850163L;

    /**
     * Checks whether the given throwable is tagged with the given tag.
     * <p>
     * This check can only succeed if the throwable is a
     * {@link TaggedIOException} and the tag is {@link Serializable}, but
     * the argument types are intentionally more generic to make it easier
     * to use this method without type casts.
     * <p>
     * A typical use for this method is in a <code>catch</code> block to
     * determine how a caught exception should be handled:
     * <pre>
     * Serializable tag = ...;
     * try {
     *     ...;
     * } catch (Throwable t) {
     *     if (TaggedIOExcepton.isTaggedWith(t, tag)) {
     *         // special processing for tagged exception
     *     } else {
     *         // handling of other kinds of exceptions
     *     }
     * }
     * </pre>
     *
     * @param throwable The Throwable object to check
     * @param tag tag object
     * @return {@code true} if the throwable has the specified tag,
     * otherwise {@code false}
     */
    public static boolean isTaggedWith(Throwable throwable, Object tag) {
        return tag != null
            && throwable instanceof TaggedIOException
            && tag.equals(((TaggedIOException) throwable).tag);
    }

    /**
     * Throws the original {@link IOException} if the given throwable is
     * a {@link TaggedIOException} decorator the given tag. Does nothing
     * if the given throwable is of a different type or if it is tagged
     * with some other tag.
     * <p>
     * This method is typically used in a <code>catch</code> block to
     * selectively rethrow tagged exceptions.
     * <pre>
     * Serializable tag = ...;
     * try {
     *     ...;
     * } catch (Throwable t) {
     *     TaggedIOExcepton.throwCauseIfTagged(t, tag);
     *     // handle other kinds of exceptions
     * }
     * </pre>
     *
     * @param throwable an exception
     * @param tag tag object
     * @throws IOException original exception from the tagged decorator, if any
     */
    public static void throwCauseIfTaggedWith(Throwable throwable, Object tag)
            throws IOException {
        if (isTaggedWith(throwable, tag)) {
            throw ((TaggedIOException) throwable).getCause();
        }
    }

    /**
     * The tag of this exception.
     */
    private final Serializable tag;

    /**
     * Creates a tagged wrapper for the given exception.
     *
     * @param original the exception to be tagged
     * @param tag tag of this exception
     */
    public TaggedIOException(IOException original, Serializable tag) {
        super(original.getMessage(), original);
        this.tag = tag;
    }

    /**
     * Returns the serializable tag object.
     *
     * @return tag object
     */
    public Serializable getTag() {
        return tag;
    }

    /**
     * Returns the wrapped exception. The only difference to the overridden
     * {@link Throwable#getCause()} method is the narrower return type.
     *
     * @return wrapped exception
     */
    @Override
    public IOException getCause() {
        return (IOException) super.getCause();
    }

}
