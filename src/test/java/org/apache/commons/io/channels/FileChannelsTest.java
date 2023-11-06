package org.apache.commons.io.channels;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.AbstractTempDirTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class FileChannelsTest extends AbstractTempDirTest {

	private static final int BUFFER_SIZE = 1024;

	@Test
	void test_contentEquals_detectsSmallDifferenceInLargeFiles() throws IOException {

		// prepare test files with same size but different content
		// (first 3 bytes are different, followed by a large amount of equal content)
		final File file1 = new File(tempDirFile, "test1.txt");
		final File file2 = new File(tempDirFile, "test2.txt");
		String equalContent = StringUtils.repeat("x", BUFFER_SIZE);
		FileUtils.writeStringToFile(file1, "ABC" + equalContent, US_ASCII);
		FileUtils.writeStringToFile(file2, "XYZ" + equalContent, US_ASCII);

		// assert: file checksums are different
		long checksum1 = FileUtils.checksumCRC32(file1);
		long checksum2 = FileUtils.checksumCRC32(file2);
		assertNotEquals(checksum1, checksum2);

		// get file channels for both files
		try (FileInputStream stream1 = new FileInputStream(file1)) {
			try (FileInputStream stream2 = new FileInputStream(file2)) {
				final FileChannel channel1 = stream1.getChannel();
				final FileChannel channel2 = stream2.getChannel();

				// test: compare content of file channels
				boolean equals = FileChannels.contentEquals(channel1, channel2, BUFFER_SIZE);

				// assert: content is not equal ("ABC..." vs "XYZ...")
				assertFalse(equals);
			}
		}
	}

}