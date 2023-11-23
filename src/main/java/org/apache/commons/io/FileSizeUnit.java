package org.apache.commons.io;

import java.text.DecimalFormat;

/**
 * File size conversion tool, supporting mutual conversion of TB, GB, MB, KB, Byte, etc.,
 * as well as converting the size of numeric types into human-readable strings.
 * @author hellozrh
 * @date 2023-11-19
 */
public enum FileSizeUnit {
    /**Byte*/
    BYTE {
        @Override
        public long toByte(long duration) {
            return duration;
        }

        @Override
        public long toKb(long duration) {
            return duration/(CKB/ CB);
        }

        @Override
        public long toMb(long duration) {
            return duration/(CMB/ CB);
        }

        @Override
        public long toGb(long duration) {
            return duration/(CGB/ CB);
        }

        @Override
        public long toTb(long duration) {
            return duration/(CTB/ CB);
        }
    },

    /**KB*/
    KB {
        @Override
        public long toByte(long duration) {
            return x(duration, CKB/ CB, MAX/(CKB/ CB));
        }

        @Override
        public long toKb(long duration) {
            return duration;
        }

        @Override
        public long toMb(long duration) {
            return duration/(CMB/CKB);
        }

        @Override
        public long toGb(long duration) {
            return duration/(CGB/CKB);
        }

        @Override
        public long toTb(long duration) {
            return duration/(CTB/CKB);
        }
    },

    /**MB*/
    MB {
        @Override
        public long toByte(long duration) {
            return x(duration, CMB/ CB, MAX/(CMB/ CB));
        }

        @Override
        public long toKb(long duration) {
            return x(duration, CMB/CKB, MAX/(CMB/CKB));
        }

        @Override
        public long toMb(long duration) {
            return duration;
        }

        @Override
        public long toGb(long duration) {
            return duration/(CGB/CMB);
        }

        @Override
        public long toTb(long duration) {
            return duration/(CTB/CMB);
        }
    },

    /**GB*/
    GB {
        @Override
        public long toByte(long duration) {
            return x(duration, CGB/ CB, MAX/(CGB/ CB));
        }

        @Override
        public long toKb(long duration) {
            return x(duration, CGB/CKB, MAX/(CGB/CKB));
        }

        @Override
        public long toMb(long duration) {
            return x(duration, CGB/CMB, MAX/(CGB/CMB));
        }

        @Override
        public long toGb(long duration) {
            return duration;
        }

        @Override
        public long toTb(long duration) {
            return duration/(CTB/CGB);
        }
    },

    /**TB*/
    TB {
        @Override
        public long toByte(long duration) {
            return x(duration, CTB/ CB, MAX/(CTB/ CB));
        }

        @Override
        public long toKb(long duration) {
            return x(duration, CTB/CKB, MAX/(CTB/CKB));
        }

        @Override
        public long toMb(long duration) {
            return x(duration, CTB/CMB, MAX/(CTB/CMB));
        }

        @Override
        public long toGb(long duration) {
            return x(duration, CTB/CGB, MAX/(CTB/CTB));
        }

        @Override
        public long toTb(long duration) {
            return duration;
        }
    }
    ;

    private static final long CB = 1L;
    private static final long CKB = 1024L;
    private static final long CMB = (CKB * CKB);
    private static final long CGB = (CKB * CMB);
    private static final long CTB = (CKB * CGB);

    private static final long MAX = Long.MAX_VALUE;

    /**
     * Scale d by m, checking for overflow.
     * This has a short name to make above code more readable.
     */
    private static long x(long d, long m, long over) {
        if (d >  over) { return Long.MAX_VALUE; }
        if (d < -over) { return Long.MIN_VALUE; }
        return d * m;
    }


    public long toByte(long duration) { throw new AbstractMethodError(); }
    public long toKb(long duration) { throw new AbstractMethodError(); }
    public long toMb(long duration) { throw new AbstractMethodError(); }
    public long toGb(long duration) { throw new AbstractMethodError(); }
    public long toTb(long duration) { throw new AbstractMethodError(); }

    /**
     * Convert file size to a human-readable string with units, such as 3.1GB, 300.0MB, etc
     * @param length the length of the file, int bytes, see as {@link java.io.File#length()}
     * @return a human-readable string with units
     */
    public static String readableSize(long length) {
        return readableSize(length, FileSizeUnit.BYTE);
    }

    /**
     * Convert file size to a human-readable string with units, such as 3.1GB, 300.0MB, etc
     * @param size the length of the file, int bytes, see as {@link java.io.File#length()}
     * @param unit file length unit
     * @return a human-readable string with units
     */
    public static String readableSize(long size, FileSizeUnit unit) {
        if(size <= 0 || unit == null) {
            return "";
        }
        long byteSize = unit.toByte(size);
        String[] fileSizeArr = getFileSize(byteSize);
        return fileSizeArr[0]+fileSizeArr[1];
    }

    /**
     * Returns the file size and array in appropriate units,
     * with String [0] as the file size number and String [1] as the unit
     * @param length the length of the file, int bytes, see as {@link java.io.File#length()}
     * @return String [0] as the file size number and String [1] as the unit
     */
    private static String[] getFileSize(long length) {
        if(length <= 0) {
            return new String[]{"0","Byte"};
        }

        String[] size = new String[2];
        DecimalFormat df = new DecimalFormat("#.0");
        if (length < CKB) {
            size[0] = df.format(length);
            size[1] = "Byte";
        } else if (length < CMB) {
            size[0] = df.format( length*1.0/ CKB);
            size[1] = "KB";
        } else if (length < CGB) {
            size[0] = df.format(length*1.0/ CMB);
            size[1] = "MB";
        } else if (length < CTB) {
            size[0] = df.format(length*1.0/ CGB);
            size[1] = "GB";
        } else {
            size[0] = df.format(length*1.0/ CTB);
            size[1] = "TB";
        }
        return size;
    }

}
