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

import java.util.Collection;
import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This is where most of the find functionality occurs. Nearly every option 
 * to find is mapped to a FileFilter, which are then chained together inside 
 * this class. 
 */
public class FindingFilter implements FileFilter {

    private Map options;
    private List filters = new LinkedList();
    private boolean daystart;

    public FindingFilter(Map options) {
        this.options = options;
        Collection entries = options.entrySet();
        Iterator itr = entries.iterator();
        while(itr.hasNext()) {
            Map.Entry entry = (Map.Entry)itr.next();
            if( entry.getKey().equals(Finder.DAYSTART) ) {
                this.daystart = true; 
                continue;
            }
            // knows that the key is a String
            filters.add( createFilter(entry.getKey().toString(), entry.getValue()) );
        }
    }

    private FileFilter createFilter(String option, Object argument) {

        boolean invert = false;
        if( option.startsWith(Finder.NOT) ) {
            invert = true;
            // knows that option is a String. Bad. Needs an Enum?
            option = option.substring(Finder.NOT.length());
        }
        if( option.equals(Finder.MIN) ) {
            return new MinFilter(option, argument, invert, this);
        }
        if( option.equals(Finder.NEWER) ) {
            return new NewerFilter(option, argument, invert);
        }
        if( option.equals(Finder.TIME) ) {
            return new TimeFilter(option, argument, invert, this);
        }
        if( option.equals(Finder.EMPTY) ) {
            return new EmptyFilter(option, argument, invert);
        }
        if( option.equals(Finder.SIZE) ) {
            return new SizeFilter(option, argument, invert);
        }
        if( option.equals(Finder.NAME) ) {
            return new NameFilter(option, argument, invert, false);
        }
        if( option.equals(Finder.INAME) ) {
            return new NameFilter(option, argument, invert, true);
        }
        if( option.equals(Finder.PATH) ) {
            return new PathFilter(option, argument, invert, false);
        }
        if( option.equals(Finder.IPATH) ) {
            return new PathFilter(option, argument, invert, true);
        }
        if( option.equals(Finder.REGEX) ) {
            return new RegexFilter(option, argument, invert, false);
        }
        if( option.equals(Finder.IREGEX) ) {
            return new RegexFilter(option, argument, invert, true);
        }
        if( option.equals(Finder.TYPE) ) {
            return new TypeFilter(option, argument, invert);
        }
        if( option.equals(Finder.HIDDEN) ) {
            return new HiddenFilter(option, argument, invert);
        }
        if( option.equals(Finder.CAN_READ) ) {
            return new CanReadFilter(option, argument, invert);
        }
        if( option.equals(Finder.CAN_WRITE) ) {
            return new CanWriteFilter(option, argument, invert);
        }
        return null;
    }

    public boolean accept(File file) {
        Iterator itr = filters.iterator();
        while(itr.hasNext()) {
            FileFilter filter = (FileFilter) itr.next();
            if(filter == null) {
                continue;
            }
            boolean result = filter.accept(file);
            if(result == false) {
                return false;
            }
        }
        return true;
    }

    public boolean isDaystartConfigured() {
        return this.daystart;
    }

    // helper method to make the inverting easier.
    // possibly the Filters should be inner classes.
    // possibly there should be an abstract FindFilter class.
    static boolean invert(boolean invert, boolean answer) {
        if(invert) {
            answer = !answer;
        }
        return answer;
    }

}

    // need to implement the daystart bits
    class MinFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private int argument;
        private FindingFilter parent;
        public MinFilter(Object option, Object argument, boolean invert, FindingFilter parent) {
            this.option = option;
            this.invert = invert;
            try {
                this.argument = Integer.parseInt(argument.toString());
            } catch(NumberFormatException nfe) {
                throw new IllegalArgumentException("Argument "+argument+" must be an integer.  ");
            }
            this.parent = parent;
        }
        public boolean accept(File file) {
            boolean daystart = this.parent.isDaystartConfigured();
            return FindingFilter.invert( this.invert,  file.lastModified() > System.currentTimeMillis() - this.argument * 60000 );
        }
    }

    class NewerFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private File argument;
        public NewerFilter(Object option, Object argument, boolean invert) {
            this.option = option;
            this.invert = invert;
            this.argument = new File(argument.toString());
        }
        public boolean accept(File file) {
            return FindingFilter.invert( this.invert,  file.lastModified() > this.argument.lastModified() );
        }
    }

    // implement daystart
    class TimeFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private int argument;
        private FindingFilter parent;
        public TimeFilter(Object option, Object argument, boolean invert, FindingFilter parent) {
            this.option = option;
            this.invert = invert;
            try {
                this.argument = Integer.parseInt(argument.toString());
            } catch(NumberFormatException nfe) {
                throw new IllegalArgumentException("Argument "+argument+" must be an integer.  ");
            }
            this.parent = parent;
        }
        public boolean accept(File file) {
            boolean daystart = this.parent.isDaystartConfigured();
            return FindingFilter.invert( this.invert,  file.lastModified() > System.currentTimeMillis() - this.argument * 60000*60*24 );
        }
    }

    class EmptyFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private boolean argument;
        public EmptyFilter(Object option, Object argument, boolean invert) {
            this.option = option;
            this.invert = invert;
            this.argument = new Boolean(argument.toString()).booleanValue();
        }
        public boolean accept(File file) {
            return FindingFilter.invert( this.invert,  (!file.isDirectory() && file.length() == 0) == this.argument );
        }
    }

    // needs to handle +5 for > 5 and -5 for < 5. Also needs 
    // to handle k, m, g, as suffixes.
    class SizeFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private int argument;
        public SizeFilter(Object option, Object argument, boolean invert) {
            this.option = option;
            this.invert = invert;
            try {
                this.argument = Integer.parseInt(argument.toString());
            } catch(NumberFormatException nfe) {
                throw new IllegalArgumentException("Argument "+argument+" must be an integer.  ");
            }
        }
        public boolean accept(File file) {
            return FindingFilter.invert( this.invert,  (int)(file.length()/512) == this.argument );
        }
    }

    class NameFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private Object argument;
        private boolean ignoreCase;
        public NameFilter(Object option, Object argument, boolean invert, boolean ignoreCase) {
            this.option = option;
            this.invert = invert;
            this.argument = argument;
            this.ignoreCase = ignoreCase;
        }
        public boolean accept(File file) {
            if(this.ignoreCase) {
                return FindingFilter.invert( this.invert,  WildcardUtils.match(file.getName().toLowerCase(), this.argument.toString().toLowerCase()) );
            } else {
                return FindingFilter.invert( this.invert,  WildcardUtils.match(file.getName(), this.argument.toString()) );
            }
        }
    }

    class PathFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private Object argument;
        private boolean ignoreCase;
        public PathFilter(Object option, Object argument, boolean invert, boolean ignoreCase) {
            this.option = option;
            this.invert = invert;
            this.argument = argument;
            this.ignoreCase = ignoreCase;
        }
        public boolean accept(File file) {
            if(this.ignoreCase) {
                return FindingFilter.invert( this.invert,  WildcardUtils.match(file.getPath().toLowerCase(), this.argument.toString().toLowerCase()) );
            } else {
                return FindingFilter.invert( this.invert,  WildcardUtils.match(file.getPath(), this.argument.toString()) );
            }
        }
    }

    class RegexFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private Object argument;
        private boolean ignoreCase;
        public RegexFilter(Object option, Object argument, boolean invert, boolean ignoreCase) {
            this.option = option;
            this.invert = invert;
            this.argument = argument;
            this.ignoreCase = ignoreCase;
        }
        public boolean accept(File file) {
            if(this.ignoreCase) {
                Pattern pattern = Pattern.compile(this.argument.toString(), Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(file.getPath());
                return FindingFilter.invert( this.invert,  matcher.matches() );
            } else {
                return FindingFilter.invert( this.invert,  file.getPath().matches(this.argument.toString()) );
            }
        }
    }

    class TypeFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private Object argument;
        public TypeFilter(Object option, Object argument, boolean invert) {
            this.option = option;
            this.invert = invert;
            if(!"d".equals(argument) && !"f".equals(argument)) {
                throw new IllegalArgumentException("Type option must be 'f' or 'd'. ");
            }
            this.argument = argument;
        }
        public boolean accept(File file) {
            if("d".equals(argument)) {
                return FindingFilter.invert( this.invert,  file.isDirectory() );
            } else
            if("f".equals(argument)) {
                return FindingFilter.invert( this.invert,  !file.isDirectory() );
            } else {
                throw new IllegalArgumentException("Type option must be 'f' or 'd'. ");
            }
        }
    }

    class HiddenFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private boolean argument;
        public HiddenFilter(Object option, Object argument, boolean invert) {
            this.option = option;
            this.invert = invert;
            this.argument = new Boolean(argument.toString()).booleanValue();
        }
        public boolean accept(File file) {
            return FindingFilter.invert( this.invert,  file.isHidden() == this.argument );
        }
    }

    class CanReadFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private boolean argument;
        public CanReadFilter(Object option, Object argument, boolean invert) {
            this.option = option;
            this.invert = invert;
            this.argument = new Boolean(argument.toString()).booleanValue();
        }
        public boolean accept(File file) {
            return FindingFilter.invert( this.invert,  file.canRead() == this.argument );
        }
    }

    class CanWriteFilter implements FileFilter {
        private Object option;
        private boolean invert;
        private boolean argument;
        public CanWriteFilter(Object option, Object argument, boolean invert) {
            this.option = option;
            this.invert = invert;
            this.argument = new Boolean(argument.toString()).booleanValue();
        }
        public boolean accept(File file) {
            return FindingFilter.invert( this.invert,  file.canWrite() == this.argument );
    }
}

