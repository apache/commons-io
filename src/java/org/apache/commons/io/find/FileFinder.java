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
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;

/**
 * Finds Files in a file system.
 *
 * Informs FindListeners whenever a Find is made, and returns the 
 * finds to the user.
 */
public class FileFinder implements Finder {

    // helper methods to handle options in String->whatever
    private static int toInt(Object obj) {
        if(obj == null) {
            return 0;
        } else
        if(obj instanceof Number) {
            return ((Number)obj).intValue();
        } else {
            String str = obj.toString();
            try {
                return Integer.parseInt(str.toString());
            } catch(NumberFormatException nfe) {
                throw new IllegalArgumentException("String argument "+str+" must be parseable as an integer.  ");
            }
        }
    }
    private static boolean toBoolean(Object obj) {
        if(obj == null) {
            return false;
        } else
        if(obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue();
        } else
        if(obj instanceof Number) {
            return ((Number)obj).intValue() != 0;
        } else {
            String str = obj.toString();
            return new Boolean(str).booleanValue();
        }
    }

    private List findListeners;

    /**
     * Find all files in the specified directory.
     */
    public File[] find(File directory) {
        return find(directory, new java.util.HashMap());
    }

    /**
     * @todo add maxdepth and mindepth somehow
     */
    public File[] find(File directory, Map options) {
        notifyDirectoryStarted(directory);

        boolean depthFirst = toBoolean(options.get(Finder.DEPTH));

        // TODO: to implement
        int maxDepth = toInt(options.get(Finder.MAXDEPTH));
        int minDepth = toInt(options.get(Finder.MINDEPTH));
        boolean ignoreHiddenDirs = toBoolean(options.get(Finder.IGNORE_HIDDEN_DIRS));

        FindingFilter filter = new FindingFilter(options);
        List list = find(directory, filter, depthFirst);
        if(filter.accept(directory)) {
            if(depthFirst) {
                list.add( directory );
            } else {
                list.add( 0, directory );
            }
        }
        File[] files = (File[]) list.toArray(new File[0]);
        notifyDirectoryFinished(directory, files);
        return files;
    }

    private List find(File directory, FindingFilter filter, boolean depthFirst) {

        // we can't use listFiles(filter) here, directories don't work correctly
        File[] list = directory.listFiles();

        if (list == null) {
            return null;
        }

        List retlist = new LinkedList();
        int sz = list.length;

        for (int i = 0; i < sz; i++) {
            File tmp = list[i];
            if(!depthFirst && filter.accept(tmp)) {
                retlist.add(tmp);
                notifyFileFound(directory,tmp);
            }
            if (tmp.isDirectory()) {
                notifyDirectoryStarted(tmp);
                List sublist = find(tmp, filter, depthFirst);
                int subsz = sublist.size();
                for (int j = 0; j < subsz; j++) {
                    retlist.add(sublist.get(j));
                }
                notifyDirectoryFinished(tmp, (File[]) sublist.toArray(new File[0]));
            }
            if(depthFirst && filter.accept(tmp)) {
                retlist.add(tmp);
                notifyFileFound(directory,tmp);
            }
        }

        return retlist;
    }
    
    /**
     * Add a FindListener.
     */
    public void addFindListener(FindListener fl) {
        if(findListeners == null) {
            findListeners = new LinkedList();
        }
        findListeners.add(fl);
    }

    /**
     * Remove a FindListener.
     */
    public void removeFindListener(FindListener fl) {
        if(findListeners != null) {
            findListeners.remove(fl);
        }
    }

    /**
     * Notify all FindListeners that a directory is being started.
     */
    public void notifyDirectoryStarted(File directory) {
        if(!directory.isDirectory()) {
            return;
        }
        if(findListeners != null) {
            FindEvent fe = new FindEvent(this,"directoryStarted",directory);
            Iterator itr = findListeners.iterator();
            while(itr.hasNext()) {
                FindListener findListener = (FindListener)itr.next();
                findListener.directoryStarted( fe );
            }
        }
    }

    /**
     * Notify all FindListeners that a directory has been finished.
     * Supplying the filenames that have been found.
     */
    public void notifyDirectoryFinished(File directory, File[] files) {
        if(!directory.isDirectory()) {
            return;
        }
        if(findListeners != null) {
            FindEvent fe = new FindEvent(this,"directoryFinished",directory,files);
            Iterator itr = findListeners.iterator();
            while(itr.hasNext()) {
                FindListener findListener = (FindListener)itr.next();
                findListener.directoryFinished( fe );
            }
        }
    }

    /**
     * Notify FindListeners that a file has been found.
     */
    public void notifyFileFound(File directory, File file) {
        if(file.isDirectory()) {
            return;
        }
        if(findListeners != null) {
            FindEvent fe = new FindEvent(this,"fileFound",directory,file);
            Iterator itr = findListeners.iterator();
            while(itr.hasNext()) {
                FindListener findListener = (FindListener)itr.next();
                findListener.fileFound( fe );
            }
        }
    }
    
}
