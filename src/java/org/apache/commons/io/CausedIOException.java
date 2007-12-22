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
 * Subclasses IOException with the {@link Throwable} constructors missing before Java 6. If you are using Java 6,
 * consider this class deprecated and use {@link IOException}.
 * 
 * @see <a href="mailto:commons-user@jakarta.apache.org">Apache Commons Users List</a>
 * @author <a href="http://commons.apache.org/io/">Apache Commons IO</a>
 * @version $Id: $
 * @since 1.4
 */
public class CausedIOException extends IOException {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with <code>cause</code> is <i>not</i> automatically incorporated in
     * this throwable's detail message.
     * </p>
     * 
     * @param message
     *            the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <code>null</code>
     *            value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public CausedIOException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message.
     * <p>
     * The message is <code>(cause==null ? null : cause.toString())</code> (which typically contains the class and
     * detail message of <code>cause</code>). This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link java.security.PrivilegedActionException}).
     * </p>
     * 
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <code>null</code>
     *            value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public CausedIOException(Throwable cause) {
        super(cause == null ? null : cause.toString());
        this.initCause(cause);
    }

}
