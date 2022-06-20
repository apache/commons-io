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

package org.apache.commons.io.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.Objects;

import org.apache.commons.io.function.IOBiFunction;

/**
 * A {@link SimpleFileVisitor} typed to a {@link Path}.
 *
 * @since 2.7
 */
public abstract class SimplePathVisitor extends SimpleFileVisitor<Path> implements PathVisitor {

    private final IOBiFunction<Path, IOException, FileVisitResult> visitFileFailedFunction;

    /**
     * Constructs a new instance.
     */
    protected SimplePathVisitor() {
        this.visitFileFailedFunction = super::visitFileFailed;
    }

    /**
     * Constructs a new instance.
     *
     * @param visitFileFailed Called on {@link #visitFileFailed(Path, IOException)}.
     */
    protected SimplePathVisitor(final IOBiFunction<Path, IOException, FileVisitResult> visitFileFailed) {
        this.visitFileFailedFunction = Objects.requireNonNull(visitFileFailed, "visitFileFailed");
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
        return visitFileFailedFunction.apply(file, exc);
    }
}
