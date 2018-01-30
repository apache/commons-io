package org.apache.commons.io;


/**
 * This exception is thrown, if an NTFS ADS name is passed to certain methods,
 * where it might affect the result. For example, if you pass a name like
 * "foo.exe:bar.txt" to {@link FilenameUtils#getExtension(String)}, then it
 * might return ".txt", which would be misleading, because the actual extension
 * would be ".txt".
 */
public class NtfsAdsNameException extends IllegalArgumentException {

	private static final long serialVersionUID = -9158109384797441214L;

	public NtfsAdsNameException(String pMessage) {
		super(pMessage);
	}
}
