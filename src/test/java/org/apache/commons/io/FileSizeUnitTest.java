package org.apache.commons.io;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileSizeUnitTest {

    @Test
    public void toByte() {
        Assertions.assertEquals(FileSizeUnit.BYTE.toByte(1), 1);
        Assertions.assertEquals(FileSizeUnit.KB.toByte(1), 2L<<9);
        Assertions.assertEquals(FileSizeUnit.MB.toByte(1), 2L<<19);
        Assertions.assertEquals(FileSizeUnit.GB.toByte(1), 2L<<29);
        Assertions.assertEquals(FileSizeUnit.TB.toByte(1), 2L<<39);
    }


    @Test
    public void toKb() {
        Assertions.assertEquals(FileSizeUnit.BYTE.toKb(1024), 1);
        Assertions.assertEquals(FileSizeUnit.KB.toKb(1), 1);
        Assertions.assertEquals(FileSizeUnit.MB.toKb(1), 2L<<9);
        Assertions.assertEquals(FileSizeUnit.GB.toKb(1), 2L<<19);
        Assertions.assertEquals(FileSizeUnit.TB.toKb(1), 2L<<29);
    }

    @Test
    public void toMb() {
        Assertions.assertEquals(FileSizeUnit.BYTE.toMb(1024*1024), 1);
        Assertions.assertEquals(FileSizeUnit.KB.toMb(1024), 1);
        Assertions.assertEquals(FileSizeUnit.MB.toMb(1), 1);
        Assertions.assertEquals(FileSizeUnit.GB.toMb(1), 2L<<9);
        Assertions.assertEquals(FileSizeUnit.TB.toMb(1), 2L<<19);
    }

    @Test
    public void toGb() {
        Assertions.assertEquals(FileSizeUnit.BYTE.toGb(1024*1024*1024), 1);
        Assertions.assertEquals(FileSizeUnit.KB.toGb(1024*1024), 1);
        Assertions.assertEquals(FileSizeUnit.MB.toGb(1024), 1);
        Assertions.assertEquals(FileSizeUnit.GB.toGb(1), 1);
        Assertions.assertEquals(FileSizeUnit.TB.toGb(1), 2L<<9);
    }

    @Test
    public void toTb() {
        Assertions.assertEquals(FileSizeUnit.BYTE.toTb(1024L*1024*1024*1024), 1);
        Assertions.assertEquals(FileSizeUnit.KB.toTb(1024*1024*1024), 1);
        Assertions.assertEquals(FileSizeUnit.MB.toTb(1024*1024), 1);
        Assertions.assertEquals(FileSizeUnit.GB.toTb(1024), 1);
        Assertions.assertEquals(FileSizeUnit.TB.toTb(1), 1);
    }

    @Test
    public void readableSize() {
        long size = 1024*1024;
        String sizeStr = FileSizeUnit.readableSize(size);
        Assertions.assertEquals("1.0MB", sizeStr);
    }

    @Test
    public void readableSizeUnit() {
        long size = 2345;
        String sizeStr = FileSizeUnit.readableSize(size, FileSizeUnit.MB);
        Assertions.assertEquals("2.3GB", sizeStr);
    }

}