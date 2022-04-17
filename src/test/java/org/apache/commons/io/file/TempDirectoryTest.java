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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class TempDirectoryTest {

    @SuppressWarnings("resource")
    @Test
    public void testCreatePath() throws IOException {
        final TempDirectory ref;
        try (final TempDirectory tempDir = TempDirectory.create(getClass().getCanonicalName())) {
            ref = tempDir;
            assertTrue(FileUtils.isEmptyDirectory(tempDir.toFile()));
        }
        assertFalse(Files.exists(ref.unwrap()));
        // Fails with a ProviderMismatchException
        // assertFalse(Files.exists(ref));
        //
        // Not quite right since we cannot pretend to be a package private interface instead of a class.
//        final Path proxy = (Path) Proxy.newProxyInstance(TempDirectory.class.getClassLoader(), new Class[] {Path.class}, new InvocationHandler() {
//            @Override
//            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
//                return method.invoke(ref.unwrap(), args);
//            }
//        });

    }

    @SuppressWarnings("resource")
    @Test
    public void testCreateString() throws IOException {
        final TempDirectory ref;
        try (final TempDirectory tempDir = TempDirectory.create(Paths.get("target"), getClass().getCanonicalName())) {
            ref = tempDir;
            assertTrue(FileUtils.isEmptyDirectory(tempDir.toFile()));
        }
        assertFalse(Files.exists(ref.unwrap()));
        // Fails with a ProviderMismatchException
        // assertFalse(Files.exists(ref));
    }

}
