package org.apache.commons.io.filefilter;

import java.io.File;

import org.apache.commons.lang.functor.Predicate;

/**
 * PredicateFileFilter is a FileFilter built on top of a Predicate.
 * This is essential for when lang.functor.PredicateUtils is used to 
 * create an AndPredicate around two FileFilters, and we need to treat 
 * that AndPredicate in a FileFilter way, for example, to pass to listFiles.
 */
public class PredicateFileFilter extends AbstractFileFilter {

    private Predicate predicate;

    public PredicateFileFilter(Predicate predicate) {
        this.predicate = predicate;
    }

    public boolean accept( File f ) {
        return this.predicate.evaluate( f );
    }

}
