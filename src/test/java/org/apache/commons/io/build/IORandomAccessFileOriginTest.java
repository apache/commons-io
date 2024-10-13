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
package org.apache.commons.io.build;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import org.apache.commons.io.IORandomAccessFile;
import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.build.AbstractOrigin.IORandomAccessFileOrigin;

/**
 * Tests {@link IORandomAccessFileOrigin}.
 *
 * @see RandomAccessFile
 */
public class IORandomAccessFileOriginTest extends AbstractOriginTest<IORandomAccessFile, IORandomAccessFileOrigin> {

    @SuppressWarnings("resource")
    @Override
    protected IORandomAccessFileOrigin newOriginRo() throws FileNotFoundException {
        return new IORandomAccessFileOrigin(RandomAccessFileMode.READ_ONLY.io(FILE_NAME_RO));
    }

    @SuppressWarnings("resource")
    @Override
    protected IORandomAccessFileOrigin newOriginRw() throws FileNotFoundException {
        return new IORandomAccessFileOrigin(RandomAccessFileMode.READ_WRITE.io(FILE_NAME_RW));
    }

}
