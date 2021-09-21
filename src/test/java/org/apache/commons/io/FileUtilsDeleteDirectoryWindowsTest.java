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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Requires Windows admin karma or you get "You do not have sufficient privilege to perform this operation."
 */
@EnabledOnOs(OS.WINDOWS)
public class FileUtilsDeleteDirectoryWindowsTest extends FileUtilsDeleteDirectoryBaseTest {

    @Override
    protected boolean setupSymlink(final File res, final File link) throws Exception {
        // create symlink
        final List<String> args = new ArrayList<>();
        args.add("cmd");
        args.add("/C");
        // Requires Windows admin karma or you get "You do not have sufficient privilege to perform this operation."
        args.add("mklink");

        if (res.isDirectory()) {
            args.add("/D");
        }

        args.add(link.getAbsolutePath());
        args.add(res.getAbsolutePath());

        final Process proc = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
        final InputStream errorStream = proc.getErrorStream();
        final int rc = proc.waitFor();
        System.err.print(IOUtils.toString(errorStream, Charset.defaultCharset()));
        System.err.flush();
        return rc == 0;
    }

}
