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

import java.io.File;
import java.io.FileFilter;

public class FileAdapter implements IFile {
	
	private File file;
	
	public FileAdapter(File file){
		this.file = file;
	}
	
	@Override
	public String getName(){
		return file.getName();
	}
	
	@Override
	public boolean exists(){
		return file.exists();
	}
	
	@Override
	public boolean isDirectory(){
		return file.isDirectory();
	}
	
	@Override
	public long lastModified(){
		return file.lastModified();
	}
	
	@Override
	public long length(){
		return file.length();
	}
	
	public File getFile(){
		return file;
	}
	
	@Override
	public IFile[] listFiles(){
		return convert(file.listFiles());
	}
	
	@Override
	public IFile[] listFiles(FileFilter fileFilter){
		return convert(file.listFiles(fileFilter));
	}
	
	private IFile[] convert(File[] input){
		if (input == null) {
			return null;
		}
		IFile[] retVal = new IFile[input.length];
		for (int i = 0; i < input.length; i++) {
			retVal[i] = new FileAdapter(input[i]);
			
		}
		return retVal;
	}
	
}
