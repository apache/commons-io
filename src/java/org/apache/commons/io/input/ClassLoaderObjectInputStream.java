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
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

/**
 * A special ObjectInputStream to handle highly transient classes hosted
 * by Avalon components that are juggling many classloaders.
 *
 * @author <a href="mailto:paul_hammant@yahoo.com">Paul Hammant</a>
 * @version $Revision$ $Date$
 */
public class ClassLoaderObjectInputStream
    extends ObjectInputStream
{
    private ClassLoader m_classLoader;

    /**
     * Constructs a new ClassLoaderObjectInputStream.
     * @param classLoader ClassLoader from which classes should be loaded.
     * @param inputStream InputStream to work on
     * @throws IOException in case of an I/O error
     * @throws StreamCorruptedException if the stream is corrupted
     */
    public ClassLoaderObjectInputStream( ClassLoader classLoader,
                                         InputStream inputStream )
        throws IOException, StreamCorruptedException
    {
        super( inputStream );
        m_classLoader = classLoader;
    }

    /** @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass) */
    protected Class resolveClass( ObjectStreamClass objectStreamClass )
        throws IOException, ClassNotFoundException
    {
        Class clazz =
            Class.forName( objectStreamClass.getName(), false, m_classLoader );

        if( null != clazz )
        {
            return clazz; // the classloader knows of the class
        }
        else
        {
            // classloader knows not of class, let the super classloader do it
            return super.resolveClass( objectStreamClass );
        }
    }
}
