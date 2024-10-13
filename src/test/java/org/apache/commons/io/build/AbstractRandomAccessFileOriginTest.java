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

import java.io.RandomAccessFile;

import org.apache.commons.io.IORandomAccessFile;
import org.apache.commons.io.build.AbstractOrigin.AbstractRandomAccessFileOrigin;
import org.apache.commons.io.build.AbstractOrigin.IORandomAccessFileOrigin;
import org.apache.commons.io.build.AbstractOrigin.RandomAccessFileOrigin;

/**
 * Tests {@link RandomAccessFileOrigin} and {@link IORandomAccessFileOrigin}.
 *
 * @param <T> the type of instances to build.
 * @param <B> the type of builder subclass.
 * @see RandomAccessFile
 * @see IORandomAccessFile
 */
public abstract class AbstractRandomAccessFileOriginTest<T extends RandomAccessFile, B extends AbstractRandomAccessFileOrigin<T, B>>
        extends AbstractOriginTest<T, B> {

}
