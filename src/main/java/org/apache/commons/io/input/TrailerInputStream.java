/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.io.IOUtils;

/**
 * Reads the underlying input stream while holding back the trailer.
 * 
 * <p>
 * "Normal" read calls read the underlying stream except the last few bytes (the trailer). The
 * trailer is updated with each read call. The trailer can be gotten by one of the copyTrailer
 * overloads.
 * </p>
 * 
 * <p>
 * It is safe to fetch the trailer at any time but the trailer will change with each read call
 * until the underlying stream is EOF.
 * </p>
 * 
 * <p>
 * Useful, e.g., for handling checksums: payload is followed by a fixed size hash, so while
 * streaming the payload the trailer finally contains the expected hash (this example needs
 * extra caution to revert actions when the final checksum match fails).
 * </p>
 */
public final class TrailerInputStream extends InputStream {

    private final  InputStream source;
    /**
     * Invariant: After every method call which exited without exception, the trailer has to be
     * completely filled.
     */
    private final byte[] trailer;

    /**
     * Constructs the TrailerInputStream and initializes the trailer buffer.
     * 
     * <p>
     * Reads exactly {@code trailerLength} bytes from {@code source}.
     * </p>
     * 
     * @param source underlying stream from which is read.
     * @param trailerLength the length of the trailer which is hold back (must be &gt;= 0).
     * @throws IOException initializing the trailer buffer failed.
     */
    public TrailerInputStream(final InputStream source, final int trailerLength)
            throws IOException {
        if (trailerLength < 0) {
            throw new IllegalArgumentException("Trailer length must be >= 0: " + trailerLength);
        }
        this.source = source;
        this.trailer = trailerLength == 0 ? IOUtils.EMPTY_BYTE_ARRAY : new byte[trailerLength];
        IOUtils.readFully(this.source, this.trailer);
    }

    @Override
    public int read() throws IOException {
        // Does exactly on source read call.
        // Copies this.trailer.length bytes if source is not EOF.
        final int newByte = this.source.read();
        if (newByte == IOUtils.EOF || this.trailer.length == 0) {
            return newByte;
        }
        final int ret = this.trailer[0];
        System.arraycopy(this.trailer, 1, this.trailer, 0, this.trailer.length - 1);
        this.trailer[this.trailer.length - 1] = (byte) newByte;
        return ret;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        // Does at most 2 IOUtils.read calls to source.
        // Copies at most 2 * this.trailer.length bytes.
        // All other bytes are directly written to the target buffer.
        if (off < 0 || len < 0 || b.length - off < len) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        final int readIntoBuffer;
        int read;
        // fist step: move trailer + read data
        // overview - b: [---------], t: [1234] --> b: [1234abcde], t: [fghi]
        if (len <= this.trailer.length) {
            // 1 IOUtils.read calls to source, copies this.trailer.length bytes
            // trailer can fill b, so only read into trailer needed
            // b: [----], trailer: [123456789] --> b: [1234], trailer: [----56789]
            System.arraycopy(this.trailer, 0, b, off, len);
            readIntoBuffer = len;
            // b: [1234], trailer: [----56789] --> b: [1234], trailer: [56789----]
            System.arraycopy(this.trailer, len, this.trailer, 0, this.trailer.length - len);
            // b: [1234], trailer: [56789----] --> b: [1234], trailer: [56789abcd]
            read = IOUtils.read(this.source, this.trailer, this.trailer.length - len, len);
        } else {
            // 1 or 2 IOUtils.read calls to source, copies this.trailer.length bytes
            // trailer smaller than b, so need to read into b and trailer
            // b: [---------], t: [1234] --> b: [1234-----], t: [----]
            System.arraycopy(this.trailer, 0, b, off, this.trailer.length);
            // b: [1234-----], t: [----] --> b: [1234abcde], t: [----]
            read = IOUtils.read(
                            this.source, b, off + this.trailer.length, len - this.trailer.length);
            readIntoBuffer = this.trailer.length + read;
            // b: [1234abcde], t: [----] --> b: [1234abcde], t: [fghi]
            if (read == len - this.trailer.length) { // don't try reading data when stream source EOF
                read += IOUtils.read(this.source, this.trailer);
            }
        }
        // if less data than requested has been read, the trailer buffer is not full
        // --> need to fill the trailer with the last bytes from b
        // (only possible if we reached EOF)
        // second step: ensure that trailer is completely filled with data
        // overview - b: [abcdefghi], t: [jk--] --> b: [abcdefg--], t: [hijk]
        final int underflow = Math.min(len - read, this.trailer.length);
        if (underflow > 0) {
            // at most this.trailer.length are copied to fill the trailer buffer
            if (underflow < this.trailer.length) {
                // trailer not completely empty, so move data to the end
                // b: [abcdefghi], t: [jk--] --> b: [abcdefghi], t: [--jk]
                System.arraycopy(
                        this.trailer, 0, this.trailer, underflow, this.trailer.length - underflow);
            }
            // fill trailer with last bytes from b
            // b: [abcdefghi], t: [--jk] --> b: [abcdefg--], t: [hijk]
            System.arraycopy(b, off + readIntoBuffer - underflow, this.trailer, 0, underflow);
        }
        // IOUtils.read reads as many bytes as possible, so reading 0 bytes means EOF.
        // Then, we have to mark this.
        return read == 0 && len != 0 ? IOUtils.EOF : read;
    }

    @Override
    public int available() throws IOException {
        return this.source.available();
    }

    @Override
    public void close() throws IOException {
        try {
            this.source.close();
        } finally {
            super.close();
        }
    }

    public int getTrailerLength() {
        return this.trailer.length;
    }

    public byte[] copyTrailer() {
        return this.trailer.clone();
    }

}
