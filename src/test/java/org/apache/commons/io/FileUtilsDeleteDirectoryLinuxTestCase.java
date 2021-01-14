/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@DisabledOnOs(OS.WINDOWS)
public class FileUtilsDeleteDirectoryLinuxTestCase extends FileUtilsDeleteDirectoryBaseTestCase {

	@Test
	public void testThrowsOnNullList() throws Exception {
		final File nested = new File(top, "nested");
		assertTrue(nested.mkdirs());

		// test wont work if we can't restrict permissions on the
		// directory, so skip it.
		assumeTrue(chmod(nested, 0, false));

		try {
			// cleanDirectory calls forceDelete
			FileUtils.deleteDirectory(nested);
			fail("expected IOException");
		} catch (final IOException e) {
			assertEquals("Unknown I/O error listing contents of directory: " + nested.getAbsolutePath(),
					e.getMessage());
		} finally {
			chmod(nested, 755, false);
			FileUtils.deleteDirectory(nested);
		}
		assertEquals(0, top.list().length);
	}

	@Test
	public void testThrowsOnCannotDeleteFile() throws Exception {
		final File nested = new File(top, "nested");
		assertTrue(nested.mkdirs());

		final File file = new File(nested, "restricted");
		FileUtils.touch(file);

		assumeTrue(chmod(nested, 500, false));

		try {
			// deleteDirectory calls forceDelete
			FileUtils.deleteDirectory(nested);
			fail("expected IOException");
		} catch (final IOException e) {
			final IOExceptionList list = (IOExceptionList) e;
			assertEquals("Cannot delete file: " + file.getAbsolutePath(), list.getCause(0).getMessage());
		} finally {
			chmod(nested, 755, false);
			FileUtils.deleteDirectory(nested);
		}
		assertEquals(0, top.list().length);
	}

	@Override
	protected boolean setupSymlink(File res, File link) throws Exception {
		// create symlink
		final List<String> args = new ArrayList<>();
		args.add("ln");
		args.add("-s");

		args.add(res.getAbsolutePath());
		args.add(link.getAbsolutePath());

		final Process proc;

		proc = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
		return proc.waitFor() == 0;
	}

	/** Only runs on Linux. */
	private boolean chmod(final File file, final int mode, final boolean recurse) throws InterruptedException {
		final List<String> args = new ArrayList<>();
		args.add("chmod");

		if (recurse) {
			args.add("-R");
		}

		args.add(Integer.toString(mode));
		args.add(file.getAbsolutePath());

		final Process proc;

		try {
			proc = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
		} catch (final IOException e) {
			return false;
		}
		return proc.waitFor() == 0;
	}
}
