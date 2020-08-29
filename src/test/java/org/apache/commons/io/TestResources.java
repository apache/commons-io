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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides access to this package's test resources.
 */
public class TestResources {

    private static final String ROOT = "/org/apache/commons/io/";

    public static File getFile(final String fileName) throws URISyntaxException {
        return new File(getURI(fileName));
    }

    public static Path getPath(final String fileName) throws URISyntaxException {
        return Paths.get(getURI(fileName));
    }

    public static URI getURI(final String fileName) throws URISyntaxException {
        return getURL(fileName).toURI();
    }

    public static URL getURL(final String fileName) {
        return TestResources.class.getResource(ROOT + fileName);
    }

    public static InputStream getInputStream(final String fileName) {
        return TestResources.class.getResourceAsStream(ROOT + fileName);
    }
}
