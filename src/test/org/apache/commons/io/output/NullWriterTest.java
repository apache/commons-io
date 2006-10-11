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
package org.apache.commons.io.output;

import junit.framework.TestCase;

/**
 * Really not a lot to do here, but checking that no 
 * Exceptions are thrown. 
 *
 * @version $Revision$
 */
public class NullWriterTest extends TestCase {

    public NullWriterTest(String name) {
        super(name);
    }

    public void testNull() {
        char[] chars = new char[] {'A', 'B', 'C'};
        NullWriter writer = new NullWriter();
        writer.write(1);
        writer.write(chars);
        writer.write(chars, 1, 1);
        writer.write("some string");
        writer.write("some string", 2, 2);
        writer.flush();
        writer.close();
    }

}
