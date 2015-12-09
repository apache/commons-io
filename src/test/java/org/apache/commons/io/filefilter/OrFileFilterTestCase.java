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

import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

public class OrFileFilterTestCase extends ConditionalFileFilterAbstractTestCase {

  private static final String DEFAULT_WORKING_PATH = "./OrFileFilterTestCase/";
  private static final String WORKING_PATH_NAME_PROPERTY_KEY = OrFileFilterTestCase.class.getName() + ".workingDirectory";

  private List<List<IOFileFilter>> testFilters;
  private List<boolean[]> testTrueResults;
  private List<boolean[]> testFalseResults;
  private List<Boolean> testFileResults;
  private List<Boolean> testFilenameResults;

  @Override
  protected IOFileFilter buildFilterUsingAdd(final List<IOFileFilter> filters) {
    final OrFileFilter filter = new OrFileFilter();
    for (IOFileFilter filter1 : filters) {
      filter.addFileFilter(filter1);
    }
    return filter;
  }

  @Override
  protected IOFileFilter buildFilterUsingConstructor(final List<IOFileFilter> filters) {
    return new OrFileFilter(filters);
  }

  @Override
  protected ConditionalFileFilter getConditionalFileFilter() {
    return new OrFileFilter();
  }

  @Override
  protected String getDefaultWorkingPath() {
    return DEFAULT_WORKING_PATH;
  }

  @Override
  protected List<boolean[]> getFalseResults() {
    return this.testFalseResults;
  }

  @Override
  protected List<Boolean> getFileResults() {
    return this.testFileResults;
  }

  @Override
  protected List<Boolean> getFilenameResults() {
    return this.testFilenameResults;
  }

  @Override
  protected List<List<IOFileFilter>>  getTestFilters() {
    return this.testFilters;
  }

  @Override
  protected List<boolean[]> getTrueResults() {
    return this.testTrueResults;
  }

  @Override
  protected String getWorkingPathNamePropertyKey() {
    return WORKING_PATH_NAME_PROPERTY_KEY;
  }

  @Before
  public void setUpTestFilters() {
    // filters
    //tests
    this.testFilters = new ArrayList<List<IOFileFilter>>();
    this.testTrueResults = new ArrayList<boolean[]>();
    this.testFalseResults = new ArrayList<boolean[]>();
    this.testFileResults = new ArrayList<Boolean>();
    this.testFilenameResults = new ArrayList<Boolean>();

    // test 0 - add empty elements
    {
      testFilters.add(0, null);
      testTrueResults.add(0, null);
      testFalseResults.add(0, null);
      testFileResults.add(0, null);
      testFilenameResults.add(0, null);
    }

    // test 1 - Test conditional or with all filters returning true
    {
      // test 1 filters
      final List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
      filters.add(trueFilters[1]);
      filters.add(trueFilters[2]);
      filters.add(trueFilters[3]);
      // test 1 true results
      final boolean[] trueResults = new boolean[] {true, false, false};
      // test 1 false results
      final boolean[] falseResults = new boolean[] {false, false, false};

      testFilters.add(1, filters);
      testTrueResults.add(1, trueResults);
      testFalseResults.add(1, falseResults);
      testFileResults.add(1, Boolean.TRUE);
      testFilenameResults.add(1, Boolean.TRUE);
    }

    // test 2 - Test conditional or with first filter returning false
    {
      // test 2 filters
      final List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
      filters.add(falseFilters[1]);
      filters.add(trueFilters[1]);
      filters.add(trueFilters[2]);
      filters.add(trueFilters[3]);
      filters.add(falseFilters[2]);
      filters.add(falseFilters[3]);
      // test 2 true results
      final boolean[] trueResults = new boolean[] {true, false, false};
      // test 2 false results
      final boolean[] falseResults = new boolean[] {true, false, false};

      testFilters.add(2, filters);
      testTrueResults.add(2, trueResults);
      testFalseResults.add(2, falseResults);
      testFileResults.add(2, Boolean.TRUE);
      testFilenameResults.add(2, Boolean.TRUE);
    }

    // test 3 - Test conditional or with second filter returning false
    {
      // test 3 filters
      final List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
      filters.add(trueFilters[1]);
      filters.add(falseFilters[1]);
      filters.add(trueFilters[2]);
      filters.add(trueFilters[3]);
      filters.add(falseFilters[2]);
      filters.add(falseFilters[3]);
      // test 3 true results
      final boolean[] trueResults = new boolean[] {true, false, false};
      // test 3 false results
      final boolean[] falseResults = new boolean[] {false, false, false};

      testFilters.add(3, filters);
      testTrueResults.add(3, trueResults);
      testFalseResults.add(3, falseResults);
      testFileResults.add(3, Boolean.TRUE);
      testFilenameResults.add(3, Boolean.TRUE);
    }

    // test 4 - Test conditional or with third filter returning false
    {
      // test 4 filters
      final List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
      filters.add(trueFilters[1]);
      filters.add(trueFilters[2]);
      filters.add(falseFilters[1]);
      filters.add(trueFilters[3]);
      filters.add(falseFilters[2]);
      filters.add(falseFilters[3]);
      // test 4 true results
      final boolean[] trueResults = new boolean[] {true, false, false};
      // test 4 false results
      final boolean[] falseResults = new boolean[] {false, false, false};

      testFilters.add(4, filters);
      testTrueResults.add(4, trueResults);
      testFalseResults.add(4, falseResults);
      testFileResults.add(4, Boolean.TRUE);
      testFilenameResults.add(4, Boolean.TRUE);
    }

    // test 5 - Test conditional or with first and third filters returning false
    {
      // test 5 filters
      final List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
      filters.add(falseFilters[1]);
      filters.add(trueFilters[1]);
      filters.add(falseFilters[2]);
      filters.add(falseFilters[3]);
      filters.add(trueFilters[2]);
      filters.add(trueFilters[3]);
      // test 5 true results
      final boolean[] trueResults = new boolean[] {true, false, false};
      // test 5 false results
      final boolean[] falseResults = new boolean[] {true, false, false};

      testFilters.add(5, filters);
      testTrueResults.add(5, trueResults);
      testFalseResults.add(5, falseResults);
      testFileResults.add(5, Boolean.TRUE);
      testFilenameResults.add(5, Boolean.TRUE);
    }

    // test 6 - Test conditional or with second and third filters returning false
    {
      final List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
      filters.add(trueFilters[1]);
      filters.add(falseFilters[1]);
      filters.add(falseFilters[2]);
      filters.add(trueFilters[2]);
      filters.add(trueFilters[3]);
      filters.add(falseFilters[3]);
      // test 6 true results
      final boolean[] trueResults = new boolean[] {true, false, false};
      // test 6 false results
      final boolean[] falseResults = new boolean[] {false, false, false};

      testFilters.add(6, filters);
      testTrueResults.add(6, trueResults);
      testFalseResults.add(6, falseResults);
      testFileResults.add(6, Boolean.TRUE);
      testFilenameResults.add(6, Boolean.TRUE);
    }

    // test 7 - Test conditional or with first and second filters returning false
    {
      final List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
      filters.add(falseFilters[1]);
      filters.add(falseFilters[2]);
      filters.add(trueFilters[1]);
      filters.add(falseFilters[3]);
      filters.add(trueFilters[2]);
      filters.add(trueFilters[3]);
      // test 7 true results
      final boolean[] trueResults = new boolean[] {true, false, false};
      // test 7 false results
      final boolean[] falseResults = new boolean[] {true, true, false};

      testFilters.add(7, filters);
      testTrueResults.add(7, trueResults);
      testFalseResults.add(7, falseResults);
      testFileResults.add(7, Boolean.TRUE);
      testFilenameResults.add(7, Boolean.TRUE);
    }

    // test 8 - Test conditional or with fourth filter returning false
    {
      final List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
      filters.add(trueFilters[1]);
      filters.add(trueFilters[2]);
      filters.add(trueFilters[3]);
      filters.add(falseFilters[1]);
      // test 8 true results
      final boolean[] trueResults = new boolean[] {true, false, false};
      // test 8 false results
      final boolean[] falseResults = new boolean[] {false, false, false};

      testFilters.add(8, filters);
      testTrueResults.add(8, trueResults);
      testFalseResults.add(8, falseResults);
      testFileResults.add(8, Boolean.TRUE);
      testFilenameResults.add(8, Boolean.TRUE);
    }

    // test 9 - Test conditional or with all filters returning false
    {
      final List<IOFileFilter> filters = new ArrayList<IOFileFilter>();
      filters.add(falseFilters[1]);
      filters.add(falseFilters[2]);
      filters.add(falseFilters[3]);
      // test 9 true results
      final boolean[] trueResults = new boolean[] {false, false, false};
      // test 9 false results
      final boolean[] falseResults = new boolean[] {true, true, true};

      testFilters.add(9, filters);
      testTrueResults.add(9, trueResults);
      testFalseResults.add(9, falseResults);
      testFileResults.add(9, Boolean.FALSE);
      testFilenameResults.add(9, Boolean.FALSE);
    }
  }
}
