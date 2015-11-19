/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.io.serialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/** A test class that uses various java.* member objects,
 *  to show which settings are necessary to deserialize
 *  those.
 */
public class MoreComplexObject implements Serializable {
    private static final long serialVersionUID = -5187124661539240729L;
    
    private final Random random = new Random(System.currentTimeMillis());
    private final String string = UUID.randomUUID().toString();
    private final Integer integer = random.nextInt();
    private final int pInt = random.nextInt();
    private final long pLong = random.nextLong();
    private final Integer [] intArray = { random.nextInt(), random.nextInt() };
    private final List<Boolean> boolList = new ArrayList<Boolean>();
    
    MoreComplexObject() {
        for(int i=0 ; i < 5; i++) {
            boolList.add(random.nextBoolean());
        }
    }

    @Override
    public String toString() {
        return string + integer + pInt + pLong + Arrays.asList(intArray) + boolList;
    }
}