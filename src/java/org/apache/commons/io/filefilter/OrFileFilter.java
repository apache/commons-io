/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.io.filefilter;

import java.io.File;

/**
 * This filter produces a logical OR of the two filters specified.
 *
 * @since Commons IO 1.0
 * @version $Revision: 1.7 $ $Date: 2003/12/30 06:55:58 $
 * 
 * @author Stephen Colebourne
 */
public class OrFileFilter extends AbstractFileFilter {
    
    /** The first filter */
    private IOFileFilter filter1;
    /** The second filter */
    private IOFileFilter filter2;

    /**
     * Constructs a new file filter that ORs the result of two other filters.
     * 
     * @param filter1  the first filter, must not be null
     * @param filter2  the second filter, must not be null
     * @throws IllegalArgumentException if either filter is null
     */
    public OrFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
        if (filter1 == null || filter2 == null) {
            throw new IllegalArgumentException("The filters must not be null");
        }
        this.filter1 = filter1;
        this.filter2 = filter2;
    }

    /**
     * Checks to see if either filter is true.
     * 
     * @param file  the File to check
     * @return true if either filter is true
     */
    public boolean accept(File file) {
        return filter1.accept(file) || filter2.accept(file);
    }
    
    /**
     * Checks to see if either filter is true.
     * 
     * @param file  the File directory
     * @param name  the filename
     * @return true if either filter is true
     */
    public boolean accept(File file, String name) {
        return filter1.accept(file, name) || filter2.accept(file, name);
    }
    
}
