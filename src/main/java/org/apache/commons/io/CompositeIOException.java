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

import java.io.IOException;
import java.util.List;

/**
 * Combines one or more IOExceptions into a single IOException.
 */
public class CompositeIOException extends IOException {
    private static final long serialVersionUID = 1L;

    private final IOException[] causes;

    public CompositeIOException(final String message, final List<IOException> causes) {
        super(message);
        this.causes = causes.toArray(new IOException[0]);
    }

    public CompositeIOException(final String message, final IOException... causes) {
        super(message);
        this.causes = causes.clone();
    }

    public IOException[] getCauses() {
        return causes.clone();
    }
}
