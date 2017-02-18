package org.apache.commons.io;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Test;

/**
 * This is used to test DirectoryUtils for correctness with flat directories
 *
 * @see DirectoryUtils
 */
public class DirectoryUtilsTestCase_Simple {

    String basePath = "src/test/resources/DirectoryUtils_equal/simple/";

    @Test
    public void compare_self() throws IOException {
        boolean checkFileContent = false;
        Path directory = FileSystems.getDefault().getPath(basePath + "directory");
        Path compareDirectory = FileSystems.getDefault().getPath(basePath + "directory");

        assertTrue(DirectoryUtils.isEqual(directory, compareDirectory, checkFileContent));
    }


    @Test
    public void compareMissingfile1() throws IOException {
        boolean checkFileContent = false;
        Path directory = FileSystems.getDefault().getPath(basePath + "directory_missing_file1");
        Path compareDirectory = FileSystems.getDefault().getPath(basePath + "directory");

        assertFalse(DirectoryUtils.isEqual(directory, compareDirectory, checkFileContent));
    }

    @Test
    public void compareMissingFile2() throws IOException {
        boolean checkFileContent = false;
        Path directory = FileSystems.getDefault().getPath(basePath + "directory");
        Path compareDirectory = FileSystems.getDefault().getPath(basePath + "missing_file2");

        assertFalse(DirectoryUtils.isEqual(directory, compareDirectory, checkFileContent));
    }

    @Test
    public void compareWithContent() throws IOException {
        boolean checkFileContent = true;
        Path directory = FileSystems.getDefault().getPath(basePath + "directory");
        Path compareDirectory = FileSystems.getDefault().getPath(basePath + "directory");

        assertTrue(DirectoryUtils.isEqual(directory, compareDirectory, checkFileContent));
    }

    @Test
    public void compareWithContentNotEqual() throws IOException {
        boolean checkFileContent = true;
        Path directory = FileSystems.getDefault().getPath(basePath + "directory");
        Path compareDirectory = FileSystems.getDefault().getPath(basePath + "file1_not_equal");

        assertFalse(DirectoryUtils.isEqual(directory, compareDirectory, checkFileContent));
    }

}
