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

import java.util.ArrayList;

/**
 * An implementation of Wildcard logic, as seen on command lines 
 * on UNIX and DOS.
 */
public class WildcardUtils {

    /**
     * See if a particular piece of text, often a filename, 
     * matches to a specified wildcard. 
     */
    public static boolean match(String text, String wildcard) {
        // split wildcard on ? and *
        // for each element of the array, find a matching block in text
        // earliest matching block counts
        String[] wcs = splitOnTokens(wildcard);
        int textIdx = 0;
        for(int i=0; i<wcs.length; i++) {
            if(textIdx == text.length()) {
                if("*".equals(wcs[i])) {
                    return true;
                }
                return wcs[i].length() == 0;
            }

            if("?".equals(wcs[i])) {
                textIdx++;
            } else
            if("*".equals(wcs[i])) {
                int nextIdx = i+1;
                if(nextIdx == wcs.length) {
                    return true;
                }
                int restartIdx = text.indexOf(wcs[nextIdx], textIdx);
                if(restartIdx == -1) {
                    return false;
                } else {
                    textIdx = restartIdx;
                }
            } else {
                if(!text.startsWith(wcs[i], textIdx)) {
                    return false;
                } else {
                    textIdx += wcs[i].length();
                }
            }
        }

        return true;
    }

    // package level so a unit test may run on this
    static String[] splitOnTokens(String text) {
        char[] array = text.toCharArray();
        if(text.indexOf("?") == -1 && text.indexOf("*") == -1) {
            return new String[] { text };
        }

        ArrayList list = new ArrayList();
        StringBuffer buffer = new StringBuffer();
        for(int i=0; i<array.length; i++) {
            if(array[i] == '?' || array[i] == '*') {
                if(buffer.length() != 0) {
                   list.add(buffer.toString());
                   buffer.setLength(0);
                }
                list.add(new String( new char[] { array[i] } ));
            } else {
                buffer.append(array[i]);
            }
        }
        if(buffer.length() != 0) {
            list.add(buffer.toString());
        }

        return (String[]) list.toArray(new String[0]);
    }
        

}
