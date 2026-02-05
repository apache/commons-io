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

/**
 * Provides classes to implement the builder pattern for IO classes.
 *
 * <p>
 * The main classes in this package are (indentation reflects hierarchy):
 * </p>
 * <ul>
 * <li>The root class {@linkplain org.apache.commons.io.build.AbstractSupplier AbstractSupplier} abstracts <em>supplying</em> an instance of type {@code T}
 * where {@code T} is unbounded. This class carries no state.
 * <ul>
 *
 * <li>{@linkplain org.apache.commons.io.build.AbstractOrigin AbstractOrigin} extends {@linkplain org.apache.commons.io.build.AbstractSupplier AbstractSupplier}
 * to abstract and wrap an <em>origin</em> for builders, where an origin is a {@code byte[]}, {@linkplain java.nio.channels.Channel Channel},
 * {@linkplain java.lang.CharSequence CharSequence}, {@linkplain java.io.File File}, {@linkplain java.io.InputStream InputStream},
 * {@linkplain org.apache.commons.io.IORandomAccessFile IORandomAccessFile}, {@linkplain java.io.OutputStream OutputStream}, {@linkplain java.nio.file.Path
 * Path}, {@linkplain java.io.RandomAccessFile RandomAccessFile}, {@linkplain java.io.Reader Reader}, {@linkplain java.net.URI URI}, or
 * {@linkplain java.io.Writer Writer}.</li>
 *
 * <li>{@linkplain org.apache.commons.io.build.AbstractOriginSupplier AbstractOriginSupplier} extends {@linkplain org.apache.commons.io.build.AbstractSupplier
 * AbstractSupplier} to abstract <em>building</em> an instance of type {@code T} where {@code T} is unbounded from a wrapped
 * {@linkplain org.apache.commons.io.build.AbstractOrigin origin}.
 *
 * <ul>
 * <li>{@linkplain org.apache.commons.io.build.AbstractStreamBuilder AbstractStreamBuilder} extends
 * {@linkplain org.apache.commons.io.build.AbstractOriginSupplier AbstractOriginSupplier} to abstract <em>building</em> a typed instance of type {@code T} where
 * {@code T} is unbounded. This class contains various properties like a buffer size, buffer size checker, a buffer size default, buffer size maximum, Charset,
 * Charset default, default size checker, and open options. A subclass may use all, some, or none of these properties in building instances of {@code T}.</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 *
 * @since 2.12.0
 */
package org.apache.commons.io.build;
