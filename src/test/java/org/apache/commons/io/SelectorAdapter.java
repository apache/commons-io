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

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

/**
 * Extends {@link Selector} with no-ops for testing.
 *
 */
public class SelectorAdapter extends Selector {

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public SelectorProvider provider() {
        return null;
    }

    @Override
    public Set<SelectionKey> keys() {
        return null;
    }

    @Override
    public Set<SelectionKey> selectedKeys() {
        return null;
    }

    @Override
    public int selectNow() throws IOException {
        return 0;
    }

    @Override
    public int select(final long timeout) throws IOException {
        return 0;
    }

    @Override
    public int select() throws IOException {
        return 0;
    }

    @Override
    public Selector wakeup() {
        return null;
    }

    @Override
    public void close() throws IOException {
    }

}
