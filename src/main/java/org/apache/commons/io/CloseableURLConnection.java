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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Delegates to a URLConnection while implementing AutoCloseable.
 */
final class CloseableURLConnection extends URLConnection implements AutoCloseable {

    static CloseableURLConnection open(final URI uri) throws IOException {
        return open(Objects.requireNonNull(uri, "uri").toURL());
    }

    static CloseableURLConnection open(final URL url) throws IOException {
        return new CloseableURLConnection(url.openConnection());
    }

    private final URLConnection urlConnection;

    CloseableURLConnection(final URLConnection urlConnection) {
        super(Objects.requireNonNull(urlConnection, "urlConnection").getURL());
        this.urlConnection = urlConnection;
    }

    @Override
    public void addRequestProperty(final String key, final String value) {
        urlConnection.addRequestProperty(key, value);
    }

    @Override
    public void close() {
        IOUtils.close(urlConnection);
    }

    @Override
    public void connect() throws IOException {
        urlConnection.connect();
    }

    @Override
    public boolean equals(final Object obj) {
        return urlConnection.equals(obj);
    }

    @Override
    public boolean getAllowUserInteraction() {
        return urlConnection.getAllowUserInteraction();
    }

    @Override
    public int getConnectTimeout() {
        return urlConnection.getConnectTimeout();
    }

    @Override
    public Object getContent() throws IOException {
        return urlConnection.getContent();
    }

    @Override
    public Object getContent(@SuppressWarnings("rawtypes") final Class[] classes) throws IOException {
        return urlConnection.getContent(classes);
    }

    @Override
    public String getContentEncoding() {
        return urlConnection.getContentEncoding();
    }

    @Override
    public int getContentLength() {
        return urlConnection.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return urlConnection.getContentLengthLong();
    }

    @Override
    public String getContentType() {
        return urlConnection.getContentType();
    }

    @Override
    public long getDate() {
        return urlConnection.getDate();
    }

    @Override
    public boolean getDefaultUseCaches() {
        return urlConnection.getDefaultUseCaches();
    }

    @Override
    public boolean getDoInput() {
        return urlConnection.getDoInput();
    }

    @Override
    public boolean getDoOutput() {
        return urlConnection.getDoOutput();
    }

    @Override
    public long getExpiration() {
        return urlConnection.getExpiration();
    }

    @Override
    public String getHeaderField(final int n) {
        return urlConnection.getHeaderField(n);
    }

    @Override
    public String getHeaderField(final String name) {
        return urlConnection.getHeaderField(name);
    }

    @Override
    public long getHeaderFieldDate(final String name, final long Default) {
        return urlConnection.getHeaderFieldDate(name, Default);
    }

    @Override
    public int getHeaderFieldInt(final String name, final int Default) {
        return urlConnection.getHeaderFieldInt(name, Default);
    }

    @Override
    public String getHeaderFieldKey(final int n) {
        return urlConnection.getHeaderFieldKey(n);
    }

    @Override
    public long getHeaderFieldLong(final String name, final long Default) {
        return urlConnection.getHeaderFieldLong(name, Default);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return urlConnection.getHeaderFields();
    }

    @Override
    public long getIfModifiedSince() {
        return urlConnection.getIfModifiedSince();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return urlConnection.getInputStream();
    }

    @Override
    public long getLastModified() {
        return urlConnection.getLastModified();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return urlConnection.getOutputStream();
    }

    @Override
    public Permission getPermission() throws IOException {
        return urlConnection.getPermission();
    }

    @Override
    public int getReadTimeout() {
        return urlConnection.getReadTimeout();
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return urlConnection.getRequestProperties();
    }

    @Override
    public String getRequestProperty(final String key) {
        return urlConnection.getRequestProperty(key);
    }

    @Override
    public URL getURL() {
        return urlConnection.getURL();
    }

    @Override
    public boolean getUseCaches() {
        return urlConnection.getUseCaches();
    }

    @Override
    public int hashCode() {
        return urlConnection.hashCode();
    }

    @Override
    public void setAllowUserInteraction(final boolean allowUserInteraction) {
        urlConnection.setAllowUserInteraction(allowUserInteraction);
    }

    @Override
    public void setConnectTimeout(final int timeout) {
        urlConnection.setConnectTimeout(timeout);
    }

    @Override
    public void setDefaultUseCaches(final boolean defaultUseCaches) {
        urlConnection.setDefaultUseCaches(defaultUseCaches);
    }

    @Override
    public void setDoInput(final boolean doInput) {
        urlConnection.setDoInput(doInput);
    }

    @Override
    public void setDoOutput(final boolean doOutput) {
        urlConnection.setDoOutput(doOutput);
    }

    @Override
    public void setIfModifiedSince(final long ifModifiedSince) {
        urlConnection.setIfModifiedSince(ifModifiedSince);
    }

    @Override
    public void setReadTimeout(final int timeout) {
        urlConnection.setReadTimeout(timeout);
    }

    @Override
    public void setRequestProperty(final String key, final String value) {
        urlConnection.setRequestProperty(key, value);
    }

    @Override
    public void setUseCaches(final boolean useCaches) {
        urlConnection.setUseCaches(useCaches);
    }

    @Override
    public String toString() {
        return urlConnection.toString();
    }

}
