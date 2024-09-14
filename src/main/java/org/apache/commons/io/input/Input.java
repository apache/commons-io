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

package org.apache.commons.io.input;

import java.io.IOException;

/**
 * Package-wide internals for input.
 */
class Input {

    /**
     * Throws an IOException on false input.
     *
     * @param isOpen whether an input is open or not.
     * @throws IOException if {@code isOpen} is false indicating an input is closed.
     */
    static void checkOpen(final boolean isOpen) throws IOException {
        if (!isOpen) {
            throw new IOException("Closed");
        }
    }

}
