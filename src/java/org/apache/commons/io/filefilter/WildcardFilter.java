package org.apache.commons.io.filefilter;

import java.io.File;
import java.util.List;
import org.apache.commons.io.WildcardUtils;

/**
 * Filters files using supplied wildcard(s).
 * <p/>
 * See org.apache.commons.io.find.WildcardUtils for wildcard matching rules
 * <p/>
 *
 * <p/>
 * e.g.
 * <pre>
 * File dir = new File(".");
 * FileFilter fileFilter = new WildcardFilter("*test*.java~*~");
 * File[] files = dir.listFiles(fileFilter);
 * for (int i = 0; i < files.length; i++) {
 *   System.out.println(files[i]);
 * }
 * </pre>
 *
 * @author Jason Anderson
 */
public class WildcardFilter extends AbstractFileFilter {

    /** The wildcards that will be used to match filenames */
    private String[] wildcards = null;

    /**
     * Construct a new wildcard filter for a single wildcard
     *
     * @param wildcard wildcard to match
     * @throws IllegalArgumentException if the pattern is null
     */
    public WildcardFilter(String wildcard) {
        if (wildcard == null) {
            throw new java.lang.IllegalArgumentException();
        }
    
        wildcards = new String[] { wildcard };
    }

    /**
     * Construct a new wildcard filter for an array of wildcards
     *
     * @param wildcards wildcards to match
     * @throws IllegalArgumentException if the pattern array is null
     */
    public WildcardFilter(String[] wildcards) {
        if (wildcards == null) {
            throw new java.lang.IllegalArgumentException();
        }
    
        this.wildcards = wildcards;
    }

    /**
     * Construct a new wildcard filter for a list of wildcards
     *
     * @param wildcards list of wildcards to match
     * @throws IllegalArgumentException if the pattern list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public WildcardFilter(List wildcards) {
        if (wildcards == null) {
            throw new java.lang.IllegalArgumentException();
        }
    
        this.wildcards = (String[]) wildcards.toArray(new String[wildcards.size()]);
    }

    /**
     * Checks to see if the filename matches one of the wildcards.
     *
     * @param dir   the file directory
     * @param name  the filename
     * @return true if the filename matches one of the wildcards
     */
    public boolean accept(File dir, String name) {
        if (dir != null && new File(dir, name).isDirectory()) {
            return false;
        }
    
        for (int i = 0; i < wildcards.length; i++) {
            if (WildcardUtils.match(name, wildcards[i])) {
                return true;
            }
        }
    
        return false;
    }

    /**
     * Checks to see if the filename matches one of the wildcards.
     *
     * @param file the file to check
     * @return true if the filename matches one of the wildcards
     */
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return false;
        }
    
        for (int i = 0; i < wildcards.length; i++) {
            if (WildcardUtils.match(file.getName(), wildcards[i])) {
                return true;
            }
        }
    
        return false;
    }

}
