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

import java.io.File;

/**
 * Event upon which notification is made to a FindListener.
 * It contains references to the Finder and the Directory in which 
 * the event occured.
 * Depending on the particular occasion, it may also contain 
 * a set of files or a file.
 */
public class FindEvent {

    private File directory;
    private Finder finder;
    private File file;
    private File[] files;
    private String type;

    public FindEvent(Finder finder, String type, File directory) {
        this.finder = finder;
        this.directory = directory;
        this.type = type;
    }
    
    public FindEvent(Finder finder, String type, File directory, File file) {
        this.finder = finder;
        this.directory = directory;
        this.file = file;
        this.type = type;
    }
    
    public FindEvent(Finder finder, String type, File directory, File[] files) {
        this.finder = finder;
        this.directory = directory;
        this.files = files;
        this.type = type;
    }

    public File getDirectory() {
        return this.directory;
    }
    
    public Finder getFinder() {
        return this.finder;
    }
    
    /**
     * File found.
     */
    public File getFile() {
        return this.file;
    }
    
    /**
     * Files found in a directory.
     */
    public File[] getFiles() {
        return this.files;
    }

    public String getType() {
        return this.type;
    }

    public String toString() {
        String str = "FindEvent - "+this.type+"; dir="+this.directory+", file="+this.file;
        if(this.files != null) {
            str += ", files="+java.util.Arrays.asList(this.files);
        }
        return str;
    }

}
