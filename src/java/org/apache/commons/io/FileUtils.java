package org.apache.commons.io;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 *
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;


/**
 * Common {@link java.io.File} manipulation routines.
 *
 * Taken from the commons-utils repo.
 * Also code from alexandria's FileUtils.
 *
 * @author <a href="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph.Reck</a>
 * @version $Id: FileUtils.java,v 1.7 2002/06/22 06:52:40 dion Exp $
 */
public class FileUtils {

    /**
     * The number of bytes in a kilobyte.
     */
    public static final int ONE_KB = 1024;

    /**
     * The number of bytes in a megabyte.
     */
    public static final int ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a gigabyte.
     */
    public static final int ONE_GB = ONE_KB * ONE_MB;

    /**
     * Returns a human-readable version of the file size (original is in
     * bytes).
     *
     * @param size The number of bytes.
     * @return     A human-readable display value (includes units).
     */
    public static String byteCountToDisplaySize(int size) {
        String displaySize;

        if (size / ONE_GB > 0) {
            displaySize = String.valueOf(size / ONE_GB) + " GB";
        } else if (size / ONE_MB > 0) {
            displaySize = String.valueOf(size / ONE_MB) + " MB";
        } else if (size / ONE_KB > 0) {
            displaySize = String.valueOf(size / ONE_KB) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }

        return displaySize;
    }

    /**
     * Returns the directory path portion of a file specification string.
     * Matches the equally named unix command.
     * @return The directory portion excluding the ending file separator.
     */
    public static String dirname(String filename) {
        int i = filename.lastIndexOf(File.separator);
        return (i >= 0 ? filename.substring(0, i) : "");
    }

    /**
     * Returns the filename portion of a file specification string.
     * @return The filename string with extension.
     */
    public static String filename(String filename) {
        int i = filename.lastIndexOf(File.separator);
        return (i >= 0 ? filename.substring(i + 1) : filename);
    }

    /**
     * Returns the filename portion of a file specification string.
     * Matches the equally named unix command.
     * @return The filename string without extension.
     */
    public static String basename(String filename) {
        return basename(filename, extension(filename));
    }

    /**
     * Returns the filename portion of a file specification string.
     * Matches the equally named unix command.
     */
    public static String basename(String filename, String suffix) {
        int i = filename.lastIndexOf(File.separator) + 1;
        int lastDot = ((suffix != null) && (suffix.length() > 0))
                ? filename.lastIndexOf(suffix) : -1;

        if (lastDot >= 0) {
            return filename.substring(i, lastDot);
        } else if (i > 0) {
            return filename.substring(i);
        } else {
            return filename; // else returns all (no path and no extension)
        }
    }

    /**
     * Returns the extension portion of a file specification string.
     * This everything after the last dot '.' in the filename (NOT including
     * the dot).
     */
    public static String extension(String filename) {
        int lastDot = filename.lastIndexOf('.');

        if (lastDot >= 0) {
            return filename.substring(lastDot + 1);
        } else {
            return "";
        }
    }

    /**
     * Check if a file exits.
     *
     * @param fileName The name of the file to check.
     * @return true if file exists.
     */
    public static boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    /**
     * Reads the contents of a file.
     *
     * @param fileName The name of the file to read.
     * @return The file contents or null if read failed.
     */
    public static String fileRead(String fileName) throws IOException {
        StringBuffer buf = new StringBuffer();

        FileInputStream in = new FileInputStream(fileName);

        int count;
        byte[] b = new byte[512];
        while ((count = in.read(b)) > 0)  // blocking read
        {
            buf.append(new String(b, 0, count));
        }

        in.close();

        return buf.toString();
    }

    /**
     * Writes data to a file. The file will be created if it does not exist.
     *
     * @param fileName The name of the file to write.
     * @param data The content to write to the file.
     */
    public static void fileWrite(String fileName, String data) throws Exception {
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(data.getBytes());
        out.close();
    }
    
    /**
     * Copy a file. The new file will be created if it does not exist. This is
     * an inefficient method, which just calls {@link #fileRead(String)} and
     * then {@link #fileWrite(String,String)}
     *
     * @param inFileName the file to copy
     * @param outFileName the file to copy to
     * @throws Exception if fileRead or fileWrite throw it
     */
    public static void fileCopy(String inFileName, String outFileName) throws
        Exception
    {
        String content = FileUtils.fileRead(inFileName);
        FileUtils.fileWrite(outFileName, content);
    }

    /**
     * Deletes a file.
     *
     * @param fileName The name of the file to delete.
     */
    public static void fileDelete(String fileName) {
        File file = new File(fileName);
        file.delete();
    }

    /**
     * Waits for NFS to propagate a file creation, imposing a timeout.
     *
     * @param fileName The name of the file.
     * @param seconds The maximum time in seconds to wait.
     * @return True if file exists.
     */
    public static boolean waitFor(String fileName, int seconds) {
        File file = new File(fileName);
        int timeout = 0;
        int tick = 0;
        while (!file.exists()) {
            if (tick++ >= 10) {
                tick = 0;
                if (timeout++ > seconds) {
                    return false;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
            } catch (Exception ex) {
                break;
            }
        }
        return true;
    }

    /**
     * Creates a file handle.
     *
     * @param fileName The name of the file.
     * @return A <code>File</code> instance.
     */
    public static File getFile(String fileName) {
        return new File(fileName);
    }

    /**
     * Given a directory and an array of extensions... return an array of
     * compliant files.
     *
     * TODO Should an ignore list be passed in?
     * TODO Should a recurse flag be passed in?
     *
     * The given extensions should be like "java" and not like ".java"
     */
    public static String[] getFilesFromExtension(String directory, String[] extensions) {

        Vector files = new Vector();

        java.io.File currentDir = new java.io.File(directory);

        String[] unknownFiles = currentDir.list();

        if (unknownFiles == null) {
            return new String[0];
        }

        for (int i = 0; i < unknownFiles.length; ++i) {
            String currentFileName = directory + System.getProperty("file.separator") + unknownFiles[i];
            java.io.File currentFile = new java.io.File(currentFileName);

            if (currentFile.isDirectory()) {


                //ignore all CVS directories...
                if (currentFile.getName().equals("CVS")) {
                    continue;
                }


                //ok... transverse into this directory and get all the files... then combine
                //them with the current list.

                String[] fetchFiles = getFilesFromExtension(currentFileName, extensions);
                files = blendFilesToVector(files, fetchFiles);

            } else {
                //ok... add the file

                String add = currentFile.getAbsolutePath();
                if (isValidFile(add, extensions)) {
                    files.addElement(add);

                }

            }
        }

        //ok... move the Vector into the files list...

        String[] foundFiles = new String[files.size()];
        files.copyInto(foundFiles);

        return foundFiles;

    }


    /**
     * Private hepler method for getFilesFromExtension()
     */
    private static Vector blendFilesToVector(Vector v, String[] files) {

        for (int i = 0; i < files.length; ++i) {
            v.addElement(files[i]);
        }

        return v;
    }

    /**
     * Checks to see if a file is of a particular type(s).
     * Note that if the file does not have an extension, an empty string
     * (&quot;&quot;) is matched for.
     *
     */
    private static boolean isValidFile(String file, String[] extensions) {


        String extension = FileUtils.extension(file);
        if (extension == null) {
            extension = "";
        }

        //ok.. now that we have the "extension" go through the current know
        //excepted extensions and determine if this one is OK.

        for (int i = 0; i < extensions.length; ++i) {
            if (extensions[i].equals(extension))
                return true;
        }

        return false;

    }

    /**
     * Simple way to make a directory
     */
    public static void mkdir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

}
