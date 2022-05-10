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
package org.apache.commons.io.watcher;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

public class WatcherProperties {
    /**
     * Array of types what kind of events should be triggered on
     */
    private WatchEvent.Kind<?>[] listenEvents;

    /**
     * Recreate watcher if event happened. If == false, then thread would die on first event.
     */
    private boolean endless;

    public WatcherProperties() {
        listenEvents = new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE};
        endless = true;
    }

    public WatchEvent.Kind<?>[] getListenEvents() {
        return listenEvents;
    }

    public void setListenEvents(WatchEvent.Kind<?>[] listenEvents) {
        this.listenEvents = listenEvents;
    }

    public boolean isEndless() {
        return endless;
    }

    public void setEndless(boolean endless) {
        this.endless = endless;
    }
}
