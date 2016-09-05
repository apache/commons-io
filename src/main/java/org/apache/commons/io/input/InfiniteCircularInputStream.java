package org.apache.commons.io.input;

import java.io.InputStream;

/**
 * 
 * An {@link InputStream} that infinitely repeats provided bytes.
 * <p>
 * Closing a <tt>InfiniteCircularInputStream</tt> has no effect. The methods in
 * this class can be called after the stream has been closed without
 * generating an <tt>IOException</tt>. 
 * 
 *
 */
public class InfiniteCircularInputStream extends InputStream {

	final private byte[] repeatedContent;
	private int position = -1;
	
	/**
     * Creates a InfiniteCircularStream from the specified array of chars.
     * @param repeatedContent       Input buffer to be repeated (not copied)
     */
	public InfiniteCircularInputStream(byte[] repeatedContent) {
		this.repeatedContent = repeatedContent;
	}

	@Override
	public int read() {
		position = (position + 1) % repeatedContent.length;
		return repeatedContent[position] & 0xff; // copied from java.io.ByteArrayInputStream.read()
	}

}
