/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
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
package org.apache.commons.io.testtools;

import java.io.IOException;
import java.io.OutputStream;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.output.ProxyOutputStream;

/**
 * Helper class for checking behaviour of IO classes.
 * 
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 */
public class YellOnFlushAndCloseOutputStream extends ProxyOutputStream {

    private boolean yellForFlush;
    private boolean yellForClose;

    /**
     * @param proxy OutputStream to delegate to.
     * @param yellForFlush True if flush() is forbidden
     * @param yellForClose True if close() is forbidden
     */
    public YellOnFlushAndCloseOutputStream(OutputStream proxy, boolean yellForFlush, boolean yellForClose) {
        super(proxy);
        this.yellForFlush = yellForFlush;
        this.yellForClose = yellForClose;
    }

    /** @see java.io.OutputStream#flush() */
    public void flush() throws IOException {
        if (yellForFlush) {
            throw new AssertionFailedError("flush() was called on OutputStream");
        }
        super.flush();
    }

    /** @see java.io.OutputStream#close() */
    public void close() throws IOException {
        if (yellForClose) {
            throw new AssertionFailedError("close() was called on OutputStream");
        }
        super.close();
    }

}
