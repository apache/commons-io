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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemNotFoundException;
import java.util.stream.Stream;

import org.apache.commons.io.input.BrokenInputStream;
import org.apache.commons.io.input.BrokenReader;
import org.apache.commons.io.output.BrokenOutputStream;
import org.apache.commons.io.output.BrokenWriter;

/**
 * Factory for parameterized tests of {@link BrokenInputStream}, {@link BrokenReader}, {@link BrokenOutputStream}, and {@link BrokenWriter}.
 */
public class BrokenTestFactories {

    /**
     * A custom Error class.
     */
    public static final class CustomError extends Error {

        private static final long serialVersionUID = 1L;

    }

    /**
     * A custom Exception class.
     */
    public static final class CustomException extends Exception {

        private static final long serialVersionUID = 1L;

    }

    /**
     * A custom RuntimeException class.
     */
    public static final class CustomRuntimeException extends RuntimeException {

        private static final long serialVersionUID = 1L;

    }

    /**
     * Creates a stream of all throwable types used in testing broken streams.
     *
     * @return a stream of all throwable types used in testing broken streams.
     */
    public static Stream<Class<? extends Throwable>> parameters() {
        // @formatter:off
        return Stream.of(
            Throwable.class,                    // JRE Root class
            Exception.class,                    // JRE Root Exception class
            IOException.class,                  // JRE Root IOException class
            FileNotFoundException.class,        // JRE IOException subclass
            RuntimeException.class,             // JRE Root RuntimeException
            FileSystemNotFoundException.class,  // JRE RuntimeException subclass in NIO
            IllegalArgumentException.class,     // JRE RuntimeException subclass
            IllegalStateException.class,        // JRE RuntimeException subclass
            Error.class,                        // JRE Error root class
            ExceptionInInitializerError.class,  // JRE Error subclass
            CustomException.class,              // Custom Exception subclass
            CustomRuntimeException.class,       // Custom RuntimeException subclass
            CustomError.class                   // Custom Error subclass
        );
        // @formatter:on
    }
}
