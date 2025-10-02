/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;

/**
 * Windows-specific test cases for {@code FileUtils} and {@code PathUtils}.
 *
 * <p>Keeping these tests in a separate class makes it clear when a test
 * was skipped due to the operating system, rather than failing for other
 * reasons.</p>
 */
@EnabledOnOs(WINDOWS)
class WindowsTest {

    private static class AclAttributesHolder implements Closeable {
        private final Path path;
        private final List<AclEntry> acl;
        private final Path parent;
        private final List<AclEntry> parentAcl;

        AclAttributesHolder(Path path) throws IOException {
            this.path = path;
            this.acl = Files.getFileAttributeView(path, AclFileAttributeView.class).getAcl();
            this.parent = path.getParent();
            this.parentAcl = parent == null ? null : Files.getFileAttributeView(parent, AclFileAttributeView.class).getAcl();
        }

        @Override
        public void close() throws IOException {
            Files.getFileAttributeView(path, AclFileAttributeView.class).setAcl(acl);
            if (parent != null) {
                Files.getFileAttributeView(parent, AclFileAttributeView.class).setAcl(parentAcl);
            }
        }
    }

    private static boolean supportsDosAndAcl(Path p) {
        return Files.getFileAttributeView(p, DosFileAttributeView.class) != null && Files.getFileAttributeView(p, AclFileAttributeView.class) != null;
    }

    private static boolean isDosReadOnly(Path p) throws IOException {
        return (Boolean) Files.getAttribute(p, "dos:readonly", LinkOption.NOFOLLOW_LINKS);
    }

    private static void denyDeleteForOwner(Path path) throws IOException {
        prependDenyForOwner(Files.getFileAttributeView(path, AclFileAttributeView.class), AclEntryPermission.DELETE);
        final Path parent = path.getParent();
        if (parent != null) {
            prependDenyForOwner(Files.getFileAttributeView(parent, AclFileAttributeView.class), AclEntryPermission.DELETE_CHILD);
        }
    }

    private static void prependDenyForOwner(AclFileAttributeView view, AclEntryPermission permission) throws IOException {
        final AclEntry denyEntry = AclEntry.newBuilder().setType(AclEntryType.DENY).setPrincipal(view.getOwner()).setPermissions(permission).build();
        final List<AclEntry> acl = new ArrayList<>(view.getAcl());
        // ACL is processed in order, so add to the start of the list
        acl.add(0, denyEntry);
        view.setAcl(acl);
    }

    @Test
    void testFileUtilsForceDeleteRestoresAttributesOnFailure(@TempDir Path tempDir) throws Exception {
        // Skip if the underlying FS doesn’t expose DOS and ACL views (e.g., some network shares).
        assumeTrue(supportsDosAndAcl(tempDir), "ACL and DOS attributes not supported");

        final Path readOnly = tempDir.resolve("read-only-file.txt");
        Files.createFile(readOnly);

        // 1) Set the DOS readonly bit (what we want to ensure is restored on failure)
        Files.setAttribute(readOnly, "dos:readonly", true);
        assertTrue(isDosReadOnly(readOnly), "Precondition: file must be DOS-readonly");

        try (AclAttributesHolder ignored = new AclAttributesHolder(readOnly)) {
            denyDeleteForOwner(readOnly);

            // 2) Attempt forced deletion; should fail with AccessDeniedException due to ACLs.
            final IOException wrappedException = assertThrows(IOException.class, () -> FileUtils.forceDelete(readOnly.toFile()),
                    "Deletion must fail because DELETE/DELETE_CHILD are denied");
            final Throwable cause = wrappedException.getCause();
            assertInstanceOf(AccessDeniedException.class, cause, "Cause must be AccessDeniedException");

            // 3) Critical assertion: even though deletion failed, the DOS readonly flag must be restored.
            assertTrue(isDosReadOnly(readOnly), "dos:readonly must be preserved/restored after failed deletion");
        }
    }

    @Test
    void testPathUtilsDeleteFileRestoresAttributesOnFailure(@TempDir Path tempDir) throws Exception {
        // Skip if the underlying FS doesn’t expose DOS and ACL views (e.g., some network shares).
        assumeTrue(supportsDosAndAcl(tempDir), "ACL and DOS attributes not supported");

        final Path readOnly = tempDir.resolve("read-only-file.txt");
        Files.createFile(readOnly);

        // 1) Set the DOS readonly bit (what we want to ensure is restored on failure)
        Files.setAttribute(readOnly, "dos:readonly", true);
        assertTrue(isDosReadOnly(readOnly), "Precondition: file must be DOS-readonly");

        try (AclAttributesHolder ignored = new AclAttributesHolder(readOnly)) {
            denyDeleteForOwner(readOnly);

            // 2) Attempt forced deletion; should fail with AccessDeniedException due to ACLs.
            final IOException wrappedException = assertThrows(IOException.class, () -> PathUtils.deleteFile(readOnly,
                    StandardDeleteOption.OVERRIDE_READ_ONLY), "Deletion must fail because DELETE/DELETE_CHILD are denied");
            final Throwable cause = wrappedException.getCause();
            assertInstanceOf(AccessDeniedException.class, cause, "Cause must be AccessDeniedException");

            // 3) Critical assertion: even though deletion failed, the DOS readonly flag must be restored.
            assertTrue(isDosReadOnly(readOnly), "dos:readonly must be preserved/restored after failed deletion");
        }
    }
}
