/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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


import java.io.IOException;

import junit.framework.TestCase;


/**
 * Really not a lot to do here, but checking that no 
 * Exceptions are thrown. 
 *
 * @author Henri Yandell (bayard at apache dot org)
 * @version $Revision: 1.2 $ $Date: 2004/02/23 05:02:25 $
 */

public class NullOutputStreamTest extends TestCase {

    public NullOutputStreamTest(String name) {
        super(name);
    }

    public void testNull() throws IOException {
        NullOutputStream nos = new NullOutputStream();
        nos.write("string".getBytes());
        nos.write("some string".getBytes(), 3, 5);
        nos.write(1);
        nos.write(0x0f);
        nos.flush();
        nos.close();
        nos.write("allowed".getBytes());
        nos.write(255);
    }

}
