/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.commons.io.find;

public interface FindListener {

    /**
     * A directory has begun to be looked at by a Finder.
     */
    public void directoryStarted(FindEvent findEvent);

    /**
     * A directory has been finished. The FindEvent will 
     * contain an array of the filenames found.
     */
    public void directoryFinished(FindEvent findEvent);

    /**
     * A file has been found. The FindEvent will contain the 
     * filename found.
     */
    public void fileFound(FindEvent findEvent);
//    public void entryFound(FindEvent findEvent);

}
