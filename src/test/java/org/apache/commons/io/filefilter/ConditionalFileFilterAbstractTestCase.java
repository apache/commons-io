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
package org.apache.commons.io.filefilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class ConditionalFileFilterAbstractTestCase
  extends IOFileFilterAbstractTestCase {

  private static final String TEST_FILE_NAME_PREFIX = "TestFile";
  private static final String TEST_FILE_TYPE = ".tst";

  protected TesterTrueFileFilter[] trueFilters;
  protected TesterFalseFileFilter[] falseFilters;

  private File file;
  private File workingPath;

  public ConditionalFileFilterAbstractTestCase(final String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
    this.workingPath = this.determineWorkingDirectoryPath(this.getWorkingPathNamePropertyKey(), this.getDefaultWorkingPath());
    this.file = new File(this.workingPath, TEST_FILE_NAME_PREFIX + 1 + TEST_FILE_TYPE);
    this.trueFilters = new TesterTrueFileFilter[4];
    this.falseFilters = new TesterFalseFileFilter[4];
    this.trueFilters[1] = new TesterTrueFileFilter();
    this.trueFilters[2] = new TesterTrueFileFilter();
    this.trueFilters[3] = new TesterTrueFileFilter();
    this.falseFilters[1] = new TesterFalseFileFilter();
    this.falseFilters[2] = new TesterFalseFileFilter();
    this.falseFilters[3] = new TesterFalseFileFilter();
  }

  public void testAdd() {
    final List<TesterTrueFileFilter> filters = new ArrayList<TesterTrueFileFilter>();
    final ConditionalFileFilter fileFilter = this.getConditionalFileFilter();
    filters.add(new TesterTrueFileFilter());
    filters.add(new TesterTrueFileFilter());
    filters.add(new TesterTrueFileFilter());
    filters.add(new TesterTrueFileFilter());
    for(int i = 0; i < filters.size(); i++) {
      assertEquals("file filters count: ", i, fileFilter.getFileFilters().size());
      fileFilter.addFileFilter(filters.get(i));
      assertEquals("file filters count: ", i+1, fileFilter.getFileFilters().size());
    }
    for (final IOFileFilter filter : fileFilter.getFileFilters()) {
      assertTrue("found file filter", filters.contains(filter));
    }
    assertEquals("file filters count", filters.size(), fileFilter.getFileFilters().size());
  }

  public void testRemove() {
    final List<TesterTrueFileFilter> filters = new ArrayList<TesterTrueFileFilter>();
    final ConditionalFileFilter fileFilter = this.getConditionalFileFilter();
    filters.add(new TesterTrueFileFilter());
    filters.add(new TesterTrueFileFilter());
    filters.add(new TesterTrueFileFilter());
    filters.add(new TesterTrueFileFilter());
    for(int i = 0; i < filters.size(); i++) {
      fileFilter.removeFileFilter(filters.get(i));
      assertTrue("file filter removed", !fileFilter.getFileFilters().contains(filters.get(i)));
    }
    assertEquals("file filters count", 0, fileFilter.getFileFilters().size());
  }

  public void testNoFilters() throws Exception {
    final ConditionalFileFilter fileFilter = this.getConditionalFileFilter();
    final File file = new File(this.workingPath, TEST_FILE_NAME_PREFIX + 1 + TEST_FILE_TYPE);
    assertFileFiltering(1, (IOFileFilter) fileFilter, file, false);
    assertFilenameFiltering(1, (IOFileFilter) fileFilter, file, false);
  }

  public void testFilterBuiltUsingConstructor() throws Exception {
    final List<List<IOFileFilter>> testFilters = this.getTestFilters();
    final List<boolean []> testTrueResults = this.getTrueResults();
    final List<boolean []> testFalseResults = this.getFalseResults();
    final List<Boolean> testFileResults = this.getFileResults();
    final List<Boolean> testFilenameResults = this.getFilenameResults();

    for(int i = 1; i < testFilters.size(); i++) {
      final List<IOFileFilter> filters = testFilters.get(i);
      final boolean[] trueResults = testTrueResults.get(i);
      final boolean[] falseResults = testFalseResults.get(i);
      final boolean fileResults = testFileResults.get(i).booleanValue();
      final boolean filenameResults = testFilenameResults.get(i).booleanValue();

      // Test conditional AND filter created by passing filters to the constructor
      final IOFileFilter filter = this.buildFilterUsingConstructor(filters);

      // Test as a file filter
      this.resetTrueFilters(this.trueFilters);
      this.resetFalseFilters(this.falseFilters);
      this.assertFileFiltering(i, filter, this.file, fileResults);
      this.assertTrueFiltersInvoked(i, trueFilters, trueResults);
      this.assertFalseFiltersInvoked(i, falseFilters, falseResults);

      // Test as a filename filter
      this.resetTrueFilters(this.trueFilters);
      this.resetFalseFilters(this.falseFilters);
      this.assertFilenameFiltering(i, filter, this.file, filenameResults);
      this.assertTrueFiltersInvoked(i, trueFilters, trueResults);
      this.assertFalseFiltersInvoked(i, falseFilters, falseResults);
    }
  }

  public void testFilterBuiltUsingAdd() throws Exception {
    final List<List<IOFileFilter>> testFilters = this.getTestFilters();
    final List<boolean[]> testTrueResults = this.getTrueResults();
    final List<boolean[]> testFalseResults = this.getFalseResults();
    final List<Boolean> testFileResults = this.getFileResults();
    final List<Boolean> testFilenameResults = this.getFilenameResults();

    for(int i = 1; i < testFilters.size(); i++) {
      final List<IOFileFilter> filters = testFilters.get(i);
      final boolean[] trueResults = testTrueResults.get(i);
      final boolean[] falseResults = testFalseResults.get(i);
      final boolean fileResults = testFileResults.get(i).booleanValue();
      final boolean filenameResults = testFilenameResults.get(i).booleanValue();

      // Test conditional AND filter created by passing filters to the constructor
      final IOFileFilter filter = this.buildFilterUsingAdd(filters);

      // Test as a file filter
      this.resetTrueFilters(this.trueFilters);
      this.resetFalseFilters(this.falseFilters);
      this.assertFileFiltering(i, filter, this.file, fileResults);
      this.assertTrueFiltersInvoked(i, trueFilters, trueResults);
      this.assertFalseFiltersInvoked(i, falseFilters, falseResults);

      // Test as a filename filter
      this.resetTrueFilters(this.trueFilters);
      this.resetFalseFilters(this.falseFilters);
      this.assertFilenameFiltering(i, filter, this.file, filenameResults);
      this.assertTrueFiltersInvoked(i, trueFilters, trueResults);
      this.assertFalseFiltersInvoked(i, falseFilters, falseResults);
    }
  }

  protected abstract ConditionalFileFilter getConditionalFileFilter();
  protected abstract IOFileFilter buildFilterUsingAdd(List<IOFileFilter> filters);
  protected abstract IOFileFilter buildFilterUsingConstructor(List<IOFileFilter> filters);
  protected abstract List<List<IOFileFilter>> getTestFilters();
  protected abstract List<boolean[]> getTrueResults();
  protected abstract List<boolean[]> getFalseResults();
  protected abstract List<Boolean> getFileResults();
  protected abstract List<Boolean> getFilenameResults();
  protected abstract String getWorkingPathNamePropertyKey();
  protected abstract String getDefaultWorkingPath();
}
