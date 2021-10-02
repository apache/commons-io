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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOCase;
import org.junit.jupiter.api.Test;

public class WildcardFileFilterTest extends AbstractFilterTest {

    @Test
    public void testWildcard() {
        IOFileFilter filter = new WildcardFileFilter("*.txt");
        assertFiltering(filter, new File("log.txt"), true);
        assertFiltering(filter, new File("log.TXT"), false);
        //
        assertFiltering(filter, new File("log.txt").toPath(), true);
        assertFiltering(filter, new File("log.TXT").toPath(), false);

        filter = new WildcardFileFilter("*.txt", IOCase.SENSITIVE);
        assertFiltering(filter, new File("log.txt"), true);
        assertFiltering(filter, new File("log.TXT"), false);
        //
        assertFiltering(filter, new File("log.txt").toPath(), true);
        assertFiltering(filter, new File("log.TXT").toPath(), false);

        filter = new WildcardFileFilter("*.txt", IOCase.INSENSITIVE);
        assertFiltering(filter, new File("log.txt"), true);
        assertFiltering(filter, new File("log.TXT"), true);
        //
        assertFiltering(filter, new File("log.txt").toPath(), true);
        assertFiltering(filter, new File("log.TXT").toPath(), true);

        filter = new WildcardFileFilter("*.txt", IOCase.SYSTEM);
        assertFiltering(filter, new File("log.txt"), true);
        assertFiltering(filter, new File("log.TXT"), WINDOWS);
        //
        assertFiltering(filter, new File("log.txt").toPath(), true);
        assertFiltering(filter, new File("log.TXT").toPath(), WINDOWS);

        filter = new WildcardFileFilter("*.txt", null);
        assertFiltering(filter, new File("log.txt"), true);
        assertFiltering(filter, new File("log.TXT"), false);
        //
        assertFiltering(filter, new File("log.txt").toPath(), true);
        assertFiltering(filter, new File("log.TXT").toPath(), false);

        filter = new WildcardFileFilter("*.java", "*.class");
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.class"), true);
        assertFiltering(filter, new File("Test.jsp"), false);
        //
        assertFiltering(filter, new File("Test.java").toPath(), true);
        assertFiltering(filter, new File("Test.class").toPath(), true);
        assertFiltering(filter, new File("Test.jsp").toPath(), false);

        filter = new WildcardFileFilter(new String[] {"*.java", "*.class"}, IOCase.SENSITIVE);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.JAVA"), false);
        //
        assertFiltering(filter, new File("Test.java").toPath(), true);
        assertFiltering(filter, new File("Test.JAVA").toPath(), false);

        filter = new WildcardFileFilter(new String[] {"*.java", "*.class"}, IOCase.INSENSITIVE);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.JAVA"), true);
        //
        assertFiltering(filter, new File("Test.java").toPath(), true);
        assertFiltering(filter, new File("Test.JAVA").toPath(), true);

        filter = new WildcardFileFilter(new String[] {"*.java", "*.class"}, IOCase.SYSTEM);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.JAVA"), WINDOWS);
        //
        assertFiltering(filter, new File("Test.java").toPath(), true);
        assertFiltering(filter, new File("Test.JAVA").toPath(), WINDOWS);

        filter = new WildcardFileFilter(new String[] {"*.java", "*.class"}, null);
        assertFiltering(filter, new File("Test.java"), true);
        assertFiltering(filter, new File("Test.JAVA"), false);
        //
        assertFiltering(filter, new File("Test.java").toPath(), true);
        assertFiltering(filter, new File("Test.JAVA").toPath(), false);

        final List<String> patternList = Arrays.asList("*.txt", "*.xml", "*.gif");
        final IOFileFilter listFilter = new WildcardFileFilter(patternList);
        assertFiltering(listFilter, new File("Test.txt"), true);
        assertFiltering(listFilter, new File("Test.xml"), true);
        assertFiltering(listFilter, new File("Test.gif"), true);
        assertFiltering(listFilter, new File("Test.bmp"), false);
        //
        assertFiltering(listFilter, new File("Test.txt").toPath(), true);
        assertFiltering(listFilter, new File("Test.xml").toPath(), true);
        assertFiltering(listFilter, new File("Test.gif").toPath(), true);
        assertFiltering(listFilter, new File("Test.bmp").toPath(), false);

        final File txtFile = new File("test.txt");
        final Path txtPath = txtFile.toPath();
        final File bmpFile = new File("test.bmp");
        final Path bmpPath = bmpFile.toPath();
        final File dirFile = new File("src/java");
        final Path dirPath = dirFile.toPath();
        assertTrue(listFilter.accept(txtFile));
        assertFalse(listFilter.accept(bmpFile));
        assertFalse(listFilter.accept(dirFile));
        //
        assertEquals(FileVisitResult.CONTINUE, listFilter.accept(txtFile.toPath(), null));
        assertEquals(FileVisitResult.TERMINATE, listFilter.accept(bmpFile.toPath(), null));
        assertEquals(FileVisitResult.TERMINATE, listFilter.accept(dirFile.toPath(), null));

        assertTrue(listFilter.accept(txtFile.getParentFile(), txtFile.getName()));
        assertFalse(listFilter.accept(bmpFile.getParentFile(), bmpFile.getName()));
        assertFalse(listFilter.accept(dirFile.getParentFile(), dirFile.getName()));
        //
        assertEquals(FileVisitResult.CONTINUE, listFilter.accept(txtPath, null));
        assertEquals(FileVisitResult.TERMINATE, listFilter.accept(bmpPath, null));
        assertEquals(FileVisitResult.TERMINATE, listFilter.accept(dirPath, null));

        assertThrows(IllegalArgumentException.class, () -> new WildcardFileFilter((String) null));
        assertThrows(IllegalArgumentException.class, () -> new WildcardFileFilter((String[]) null));
        assertThrows(IllegalArgumentException.class, () -> new WildcardFileFilter((List<String>) null));
    }
}
