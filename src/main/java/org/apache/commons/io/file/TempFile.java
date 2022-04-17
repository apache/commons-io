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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

public class TempFile extends CloseablePath {

    public static TempFile create(final Path dir, final String prefix, final String suffix, final FileAttribute<?>... attrs) throws IOException {
        return new TempFile(Files.createTempFile(dir, prefix, suffix, attrs));
    }

    public static TempFile create(final String prefix, final String suffix, final FileAttribute<?>... attrs) throws IOException {
        return new TempFile(Files.createTempFile(prefix, suffix, attrs));
    }

    protected TempFile(final Path path) {
        super(path);
    }

}
