package org.apache.commons.io.filefilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFilterTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

public class FileFilter2Test extends AbstractFilterTest {

    @Test
    void filefilter() {
        File root = new File("src");
        Collection<File> allJava = FileUtils.listFiles(root, FileFilterUtils.suffixFileFilter("java"), FileFilterUtils.falseFileFilter());
        for (File f : allJava) {
            System.err.println(f.getName());
        }

        Collection<File> subdirs = FileUtils.listFilesAndDirs(root, FileFilterUtils.falseFileFilter(),FileFilterUtils.prefixFileFilter("d"));

        for (File f : subdirs) {
            System.err.println(f.getName());
        }
    }
}