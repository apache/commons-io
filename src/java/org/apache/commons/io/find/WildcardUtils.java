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
        String[] wcs = splitOnTokens(wildcard);
  
        int textIdx = 0;
        int wcsIdx = 0;
        boolean anyChars = false;
  
        // loop whilst tokens and text left to process
        while (wcsIdx < wcs.length && textIdx < text.length()) {
  
            // ? so move to next text char
            if (wcs[wcsIdx].equals("?")) {
                textIdx++;
            } else
            if (!wcs[wcsIdx].equals("*")) {
                // matching text token
                if (anyChars) {
                    // any chars then try to locate text token
                    textIdx = text.indexOf(wcs[wcsIdx], textIdx);
  
                    if (textIdx == -1) {
                        // token not found
                        return false;
                    }
                } else {
                    // matching from current position
                    if (!text.startsWith(wcs[wcsIdx], textIdx)) {
                        // couldnt match token
                        return false;
                    }
                }
  
                // matched text token, move text index to end of matched token
                textIdx += wcs[wcsIdx].length();
            }
  
            // set any chars status
            anyChars = wcs[wcsIdx].equals("*");
  
            wcsIdx++;
        }

        // didnt match all wildcards
        if (wcsIdx < wcs.length) {
            // ok if one remaining and wildcard or empty
            if (wcsIdx + 1 != wcs.length || !(wcs[wcsIdx].equals("*") || wcs[wcsIdx].equals("")) ) {
                return false;
            }
        }
  
        // ran out of text chars
        if (textIdx > text.length()) {
           return false;
        }
  
        // didnt match all text chars, only ok if any chars set
        if (textIdx < text.length() && !anyChars) {
            return false;
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
