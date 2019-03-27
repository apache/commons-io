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
package org.apache.commons.io.monitor;

public interface IFileAlterationObserver<F, D> {
	
	/**
	 * Initialize the observer.
	 *
	 * @throws Exception
	 *             if an error occurs
	 */
	void initialize() throws Exception;
	
	/**
	 * Check whether the file and its children have been created, modified or deleted.
	 */
	void checkAndNotify();
	
	/**
	 * Final processing.
	 *
	 * @throws Exception
	 *             if an error occurs
	 */
	void destroy() throws Exception;
	
	/**
	 * Add a file system listener.
	 *
	 * @param listener
	 *            The file system listener
	 */
	public void addListener(FileAlterationListener<F, D> listener);
	
	/**
	 * Remove a file system listener.
	 *
	 * @param listener
	 *            The file system listener
	 */
	public void removeListener(FileAlterationListener<F, D> listener);
	
    /**
     * Returns the set of registered file system listeners.
     *
     * @return The file system listeners
     */
	public Iterable<FileAlterationListener<F, D>> getListeners();
}
