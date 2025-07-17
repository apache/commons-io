/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.monitor;
import java.io.File;

/**
 * Receives events of file system modifications.
 * <p>
 * Register {@link FileAlterationListener}s with a {@link FileAlterationObserver}.
 * </p>
 *
 * @see FileAlterationObserver
 * @since 2.0
 */
public interface FileAlterationListener {

    /**
     * Directory changed Event.
     *
     * @param directory The directory changed
     */
    void onDirectoryChange(File directory);

    /**
     * Directory created Event.
     *
     * @param directory The directory created
     */
    void onDirectoryCreate(File directory);

    /**
     * Directory deleted Event.
     *
     * @param directory The directory deleted
     */
    void onDirectoryDelete(File directory);

    /**
     * File changed Event.
     *
     * @param file The file changed
     */
    void onFileChange(File file);

    /**
     * File created Event.
     *
     * @param file The file created
     */
    void onFileCreate(File file);

    /**
     * File deleted Event.
     *
     * @param file The file deleted
     */
    void onFileDelete(File file);

    /**
     * File system observer started checking event.
     *
     * @param observer The file system observer
     */
    void onStart(FileAlterationObserver observer);

    /**
     * File system observer finished checking event.
     *
     * @param observer The file system observer
     */
    void onStop(FileAlterationObserver observer);
}
