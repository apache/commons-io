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

import org.apache.commons.io.watcher.WatcherException;
import org.apache.commons.io.watcher.WatcherProperties;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FileWatcher {

    private static List<WatchThread> WATCH_THREADS = new ArrayList<>();

    private FileWatcher() {
    }

    /**
     * @param path Path to watching file or directory
     * @param eventConsumer Callback for executing code if event happened
     */
    public static void createWatcher(Path path,
                                         Consumer<WatchEvent<?>> eventConsumer) {
        createWatcher(path, new WatcherProperties(), eventConsumer);
    }

    /**
     * @param path Path to watching file or directory
     * @param watcherProperties Properties (filters) for trigger
     * @param eventConsumer Callback for executing code if event happened
     */
    public static void createWatcher(Path path,
                                         WatcherProperties watcherProperties,
                                         Consumer<WatchEvent<?>> eventConsumer) {
        WatchThread watchThread = new WatchThread(path, watcherProperties, eventConsumer);;
        watchThread.start();
    }


    /**
     * @return active watcher-threads
     */
    public static List<WatchThread> getWatchThreads() {
        return new ArrayList<>(WATCH_THREADS);
    }

    /**
     * @param watchThread kill watcher-thread
     */
    public static void killWatchThread(WatchThread watchThread) {
        watchThread.interrupt();
        WATCH_THREADS.remove(watchThread);
    }

    /**
     * Kill all watcher-threads
     */
    public static void killAllWatchThreads() {
        WATCH_THREADS.forEach(watchThread -> watchThread.interrupt());
        WATCH_THREADS = new ArrayList<>();
    }

    public static class WatchThread extends Thread {

        private WatcherProperties watcherProperties;
        private Consumer<WatchEvent<?>> eventConsumer;
        private Path path;

        private WatchThread(Path path,
                           WatcherProperties watcherProperties,
                           Consumer<WatchEvent<?>> event) {
            super();
            this.watcherProperties = watcherProperties;
            this.eventConsumer = event;
            this.path = path;
        }

        @Override
        public void run() {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                WATCH_THREADS.add(this);
                WatchKey watchKey = (Files.isDirectory(path) ? path : path.getParent())
                        .register(watchService, watcherProperties.getListenEvents());
                WatchKey wk = watchService.take();
                boolean processed = false;
                for (WatchEvent<?> event : wk.pollEvents()) {
                    eventConsumer.accept(event);
                    processed = true;
                }
                if (processed) {
                    if (watcherProperties.isEndless()) {
                        FileWatcher.createWatcher(path, watcherProperties, eventConsumer);
                    }
                    watchService.close();
                    WATCH_THREADS.remove(this);
                }
            } catch (InterruptedException ignored) {
            } catch (Throwable e) {
                WATCH_THREADS.remove(this);
                throw new WatcherException(e);
            }
        }
    }
}
