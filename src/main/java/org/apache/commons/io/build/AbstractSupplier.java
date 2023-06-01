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

package org.apache.commons.io.build;

import org.apache.commons.io.function.IOSupplier;

/**
 * Abstracts supplying an instance of {@code T}. Use to implement the builder pattern.
 *
 * @param <T> the type of instances to build.
 * @param <B> the type of builder subclass.
 * @since 2.12.0
 */
public abstract class AbstractSupplier<T, B extends AbstractSupplier<T, B>> implements IOSupplier<T> {

    /**
     * Returns this instance typed as the proper subclass type.
     *
     * @return this instance typed as the proper subclass type.
     */
    @SuppressWarnings("unchecked")
    protected B asThis() {
        return (B) this;
    }

}
