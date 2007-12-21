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

/**
 * Subclasses IOException with the {@link Throwable} constructor was missing before Java 6.
 * 
 * @see <a href="mailto:commons-user@jakarta.apache.org">Apache Commons Users List</a>
 * @author <a href="http://commons.apache.org/io/">Apache Commons IO</a>
 * @version $Id: $
 */
public class CausedIOException extends IOException {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an instance with the given message and cause.
     * <p>
     * This constructor was not added in the underlying {@link IOException} class until Java 6. This is a convenience
     * method which uses the {@link #initCause(Throwable)} method to set the root cause.
     * 
     * @param message
     *            exception message
     * @param cause
     *            root cause
     */
    public CausedIOException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }

}
