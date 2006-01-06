/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.commons.io;

import java.util.Iterator;

/**
 * An <code>Iterator</code> for IO objects, such as streams, readers
 * and writers, which must be closed to avoid resource leaks.
 *
 * @author Niall Pemberton
 * @version $Id$
 * @since Commons IO 1.2
 */
public interface IOIterator extends Iterator {

    /**
     * Close any open io resources, exceptions are quitely swallowed.
     */
    public void close();

}
