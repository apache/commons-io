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
import junit.framework.TestCase;

public abstract class IOFileFilterAbstractTestCase extends TestCase {
  
  /**
   * Constructs a new instance of
   * <code>IOFileFilterAbstractTestCase</code>.
   */
  public IOFileFilterAbstractTestCase(final String name) {
    super(name);
  }
  
  public boolean assertFileFiltering(final int testNumber, final IOFileFilter filter, final File file, final boolean expected)
  throws Exception {
    assertTrue(
    "test " + testNumber + " Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file,
    (filter.accept(file) == expected));
    return true; // return is irrelevant
  }

  public boolean assertFilenameFiltering(final int testNumber, final IOFileFilter filter, final File file, final boolean expected)
  throws Exception {
    // Assumes file has parent and is not passed as null
    assertTrue(
    "test " + testNumber + " Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for " + file,
    (filter.accept(file.getParentFile(), file.getName()) == expected));
    return true; // return is irrelevant
  }

  public void assertFiltering(final int testNumber, final IOFileFilter filter, final File file, final boolean expected)
  throws Exception {
    // Note. This only tests the (File, String) version if the parent of
    //       the File passed in is not null
    assertTrue(
    "test " + testNumber + " Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file,
    (filter.accept(file) == expected));

    if (file != null && file.getParentFile() != null) {
      assertTrue(
      "test " + testNumber + " Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for " + file,
      (filter.accept(file.getParentFile(), file.getName()) == expected));
    } 
    else if (file == null) {
      assertTrue(
      "test " + testNumber + " Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for null",
      filter.accept(file) == expected);
    }
  }

  public void assertTrueFiltersInvoked(final int testNumber, final TesterTrueFileFilter[] filters, final boolean[] invoked) {
    for(int i = 1; i < filters.length; i++) {
      assertEquals("test " + testNumber + " filter " + i + " invoked", invoked[i-1], filters[i].isInvoked());
    }
  }
  
  public void assertFalseFiltersInvoked(final int testNumber, final TesterFalseFileFilter[] filters, final boolean[] invoked) {
    for(int i = 1; i < filters.length; i++) {
      assertEquals("test " + testNumber + " filter " + i + " invoked", invoked[i-1], filters[i].isInvoked());
    }
  }
  
  public File determineWorkingDirectoryPath(final String key, final String defaultPath) {
    // Look for a system property to specify the working directory
    String workingPathName = System.getProperty(key, defaultPath);
    return new File(workingPathName);
  }

  public void resetFalseFilters(TesterFalseFileFilter[] filters) {
    for(int i = 0; i < filters.length; i++) {
      if(filters[i] != null) {
        filters[i].reset();
      }
    }
  }
  
  public void resetTrueFilters(TesterTrueFileFilter[] filters) {
    for(int i = 0; i < filters.length; i++) {
      if(filters[i] != null) {
        filters[i].reset();
      }
    }
  }
  
  class TesterTrueFileFilter extends TrueFileFilter {

    private boolean invoked;
    
    public boolean accept(File file) {
      setInvoked(true);
      return super.accept(file);
    }
    
    public boolean accept(File file, String str) {
      setInvoked(true);
      return super.accept(file, str);
    }
    
    public boolean isInvoked() {
      return this.invoked;
    }

    public void setInvoked(boolean invoked) {
      this.invoked = invoked;
    }
    
    public void reset() {
      setInvoked(false);
    }
  }
  
  class TesterFalseFileFilter extends FalseFileFilter {
    
    private boolean invoked;
    
    public boolean accept(File file) {
      setInvoked(true);
      return super.accept(file);
    }
    
    public boolean accept(File file, String str) {
      setInvoked(true);
      return super.accept(file, str);
    }
    
    public boolean isInvoked() {
      return this.invoked;
    }

    public void setInvoked(boolean invoked) {
      this.invoked = invoked;
    }
    
    public void reset() {
      setInvoked(false);
    }
  }
}
