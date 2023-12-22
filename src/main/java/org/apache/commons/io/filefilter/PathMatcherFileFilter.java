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

package org.apache.commons.io.filefilter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;

/**
 * Delegates matching to a {@link PathMatcher}.
 *
 * @since 2.14.0
 */
public class PathMatcherFileFilter extends AbstractFileFilter {

    private final PathMatcher pathMatcher;

    /**
     * Constructs a new instance to perform matching with a PathMatcher.
     *
     * @param pathMatcher The PathMatcher delegate.
     */
    public PathMatcherFileFilter(final PathMatcher pathMatcher) {
        this.pathMatcher = Objects.requireNonNull(pathMatcher, "pathMatcher");
    }

    @Override
    public boolean accept(final File file) {
        return file != null && matches(file.toPath());
    }

    @Override
    public boolean matches(final Path path) {
        return pathMatcher.matches(path);
    }
}
