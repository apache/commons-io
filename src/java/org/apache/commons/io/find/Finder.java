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
import java.util.Map;

/**
 * A Finder of Files. Though the structure the files are in is 
 * unspecified.
 */
public interface Finder {

    // MODIFIER
    public static final String NOT = "NOT_";

    // OPTIONS
    public static final String DAYSTART = "DAYSTART";
    public static final String DEPTH = "DEPTH";
    public static final String MAXDEPTH = "MAXDEPTH";
    public static final String MINDEPTH = "MINDEPTH";
    public static final String IGNORE_HIDDEN_DIRS = "IGNORE_HIDDEN_DIRS";

    // Time based tests
    public static final String MIN = "MIN";
    public static final String NEWER = "NEWER";
    public static final String TIME = "TIME";

    // size based tests
    public static final String EMPTY = "EMPTY";
    public static final String SIZE = "SIZE";

    // name based tests
    public static final String NAME = "NAME";
    public static final String INAME = "INAME";
    public static final String PATH = "PATH";
    public static final String IPATH = "IPATH";
    public static final String REGEX = "REGEX";
    public static final String IREGEX = "IREGEX";

    // type of file
    public static final String TYPE = "TYPE";      // supports 'd' and 'f'
    public static final String HIDDEN = "HIDDEN";

    // permission replacements
    public static final String CAN_READ = "CAN_READ";
    public static final String CAN_WRITE = "CAN_WRITE";

    public void addFindListener(FindListener fl);
    public void removeFindListener(FindListener fl);

    public File[] find(File root);
    public File[] find(File root, Map options);

}
