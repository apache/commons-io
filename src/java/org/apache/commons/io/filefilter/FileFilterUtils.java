package org.apache.commons.io.filefilter;

import org.apache.commons.lang.functor.Predicate;
import org.apache.commons.lang.functor.PredicateUtils;

public class FileFilterUtils {

    public FileFilterUtils() {
    }

    static public FileFilter predicateToFileFilter(Predicate predicate) {
        return new PredicateFileFilter(predicate);
    }

    static public FileFilter andFileFilter(FileFilter f1, FileFilter f2) {
        return predicateToFileFilter(
            PredicateUtils.andPredicate( f1, f2 ) 
            );
    }

    static public FileFilter orFileFilter(FileFilter f1, FileFilter f2) {
        return predicateToFileFilter(
            PredicateUtils.orPredicate( f1, f2 ) 
            );
    }

    static public FileFilter nullFileFilter() {
        return predicateToFileFilter( PredicateUtils.nullPredicate() );
    }

    static public FileFilter notFileFilter(FileFilter f) {
        return predicateToFileFilter( PredicateUtils.notPredicate( f ) );
    }

}
