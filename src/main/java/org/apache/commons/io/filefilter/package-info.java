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

/**
 * Provides the an interface {@link org.apache.commons.io.filefilter.IOFileFilter IOFileFilter} that combines both
 * {@link java.io.FileFilter} and {@link java.io.FilenameFilter}.
 * <p>Besides that the package offers a series of ready-to-use implementations of the
 * IOFileFilter interface including implementation that allow you to combine
 * other such filters.</p>
 * <p>These filter can be used to list files or in {@link java.awt.FileDialog},
 * for example.</p>
 * <table>
 * <caption>There are a number of 'primitive' filters:</caption>
 * <tbody>
 * <tr>
 * <td><a href="DirectoryFileFilter.html">DirectoryFilter</a></td>
 * <td>Only accept directories</td>
 * </tr>
 * <tr>
 * <td><a href="PrefixFileFilter.html">PrefixFileFilter</a></td>
 * <td>Filter based on a prefix</td>
 * </tr>
 * <tr>
 * <td><a href="SuffixFileFilter.html">SuffixFileFilter</a></td>
 * <td>Filter based on a suffix</td>
 * </tr>
 * <tr>
 * <td><a href="NameFileFilter.html">NameFileFilter</a></td>
 * <td>Filter based on a file name</td>
 * </tr>
 * <tr>
 * <td><a href="WildcardFileFilter.html">WildcardFileFilter</a></td>
 * <td>Filter based on wildcards</td>
 * </tr>
 * <tr>
 * <td><a href="AgeFileFilter.html">AgeFileFilter</a></td>
 * <td>Filter based on last modified time of file</td>
 * </tr>
 * <tr>
 * <td><a href="SizeFileFilter.html">SizeFileFilter</a></td>
 * <td>Filter based on file size</td>
 * </tr>
 * </tbody>
 * </table>
 * <table>
 * <caption>And there are five 'boolean' filters:</caption>
 * <tbody>
 * <tr>
 * <td><a href="TrueFileFilter.html">TrueFileFilter</a></td>
 * <td>Accept all files</td>
 * </tr>
 * <tr>
 * <td><a href="FalseFileFilter.html">FalseFileFilter</a></td>
 * <td>Accept no files</td>
 * </tr>
 * <tr>
 * <td><a href="NotFileFilter.html">NotFileFilter</a></td>
 * <td>Applies a logical NOT to an existing filter</td>
 * </tr>
 * <tr>
 * <td><a href="AndFileFilter.html">AndFileFilter</a></td>
 * <td>Combines two filters using a logical AND</td>
 * </tr>
 * <tr>
 * <td><a href="OrFileFilter.html">OrFileFilter</a></td>
 * <td>Combines two filter using a logical OR</td>
 * </tr>
 *
 * </tbody>
 * </table>
 * <h2>Using Classic IO</h2>
 * <p>These boolean FilenameFilters can be nested, to allow arbitrary expressions.
 * For example, here is how one could print all non-directory files in the
 * current directory, starting with "A", and ending in ".java" or ".class":</p>
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(
 * new AndFileFilter(
 * new AndFileFilter(
 * new PrefixFileFilter("A"),
 * new OrFileFilter(
 * new SuffixFileFilter(".class"),
 * new SuffixFileFilter(".java")
 * )
 * ),
 * new NotFileFilter(
 * new DirectoryFileFilter()
 * )
 * )
 * );
 * for (int i=0; i&lt;files.length; i++) {
 * System.out.println(files[i]);
 * }
 * </pre>
 * <p>
 * You can alternatively build a filter tree using the "and", "or", and "not" methods on filters themselves:
 * </p>
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(
 * new AndFileFilter(
 * new PrefixFileFilter("A").and(
 * new SuffixFileFilter(".class").or(new SuffixFileFilter(".java"))),
 * new DirectoryFileFilter().not()
 * )
 * );
 * for (int i=0; i&lt;files.length; i++) {
 * System.out.println(files[i]);
 * }
 * </pre>
 * <p>This package also contains a utility class:
 * <a href="FileFilterUtils.html">FileFilterUtils</a>. It allows you to use all
 * file filters without having to put them in the import section. Here's how the
 * above example will look using FileFilterUtils:</p>
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(
 * FileFilterUtils.andFileFilter(
 * FileFilterUtils.andFileFilter(
 * FileFilterUtils.prefixFileFilter("A"),
 * FileFilterUtils.orFileFilter(
 * FileFilterUtils.suffixFileFilter(".class"),
 * FileFilterUtils.suffixFileFilter(".java")
 * )
 * ),
 * FileFilterUtils.notFileFilter(
 * FileFilterUtils.directoryFileFilter()
 * )
 * )
 * );
 * for (int i=0; i&lt;files.length; i++) {
 * System.out.println(files[i]);
 * }
 * </pre>
 * <h2>Using NIO</h2>
 * <p>You can combine Java <b>file tree walking</b> by using <code>java.nio.file.Files.walk()</code> APIs with filters:</p>
 * <pre>
 * final Path dir = Paths.get("");
 * // We are interested in files older than one day
 * final long cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
 * final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(new AgeFileFilter(cutoff));
 * //
 * // Walk one dir
 * Files.<b>walkFileTree</b>(dir, Collections.emptySet(), 1, visitor);
 * System.out.println(visitor.getPathCounters());
 * System.out.println(visitor.getFileList());
 * //
 * visitor.getPathCounters().reset();
 * //
 * // Walk dir tree
 * Files.<b>walkFileTree</b>(dir, visitor);
 * System.out.println(visitor.getPathCounters());
 * System.out.println(visitor.getDirList());
 * System.out.println(visitor.getFileList());
 * </pre>
 * <p>There are a few other goodies in that class so please have a look at the
 * documentation in detail.</p>
 */
package org.apache.commons.io.filefilter;
