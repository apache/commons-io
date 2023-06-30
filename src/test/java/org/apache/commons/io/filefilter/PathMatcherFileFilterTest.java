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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.apache.commons.io.FileSystem;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link PathMatcherFileFilter}.
 */
public class PathMatcherFileFilterTest extends AbstractFilterTest {

    @Test
    public void testGlob() throws IOException {
        @SuppressWarnings("resource")
        final IOFileFilter filter = new PathMatcherFileFilter(FileSystems.getDefault().getPathMatcher("glob:*.txt"));
        final File file1 = new File("log.txt");
        final File file2 = new File("log.TXT");
        //
        assertTrue(filter.accept(file1));
        assertEquals(!FileSystem.getCurrent().isCaseSensitive(), filter.accept(file2));
        assertTrue(filter.accept(file1.getParentFile(), file1.getName()));
        assertEquals(!FileSystem.getCurrent().isCaseSensitive(), filter.accept(file2.getParentFile(), file2.getName()));
        assertFiltering(filter, file1, true);
        assertFiltering(filter, file1.toPath(), true);
    }

}
