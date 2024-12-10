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
 * Abstracts supplying an instance of {@code T}.
 * <p>
 * Extend this class to implement the builder pattern.
 * </p>
 * <p>
 * For example, here is a builder, a domain class, and a test.
 * </p>
 * <p>
 * The builder:
 * </p>
 * <pre>
    &#47;**
     &#42; Builds Foo instances.
     &#42;&#47;
    public static class Builder extends AbstractSupplier&#60;Foo, Builder&#62; {

        private String bar1;
        private String bar2;
        private String bar3;

        &#47;**
         &#42; Builds a new Foo.
         &#42;&#47;
        &#64;Override
        public Foo get() {
            return new Foo(bar1, bar2, bar3);
        }

        public Builder setBar1(final String bar1) {
            this.bar1 = bar1;
            return this;
        }

        public Builder setBar2(final String bar2) {
            this.bar2 = bar2;
            return this;
        }

        public Builder setBar3(final String bar3) {
            this.bar3 = bar3;
            return this;
        }
    }
 * </pre>
 * <p>
 * The domain class:
 * </p>
 * <pre>
    &#47;**
     &#42; Domain class.
     &#42;&#47;
    public class Foo {

        public static Builder builder() {
            return new Builder();
        }

        private final String bar1;
        private final String bar2;
        private final String bar3;

        private Foo(final String bar1, final String bar2, final String bar3) {
            this.bar1 = bar1;
            this.bar2 = bar2;
            this.bar3 = bar3;
        }

        public String getBar1() {
            return bar1;
        }

        public String getBar2() {
            return bar2;
        }

        public String getBar3() {
            return bar3;
        }

    }
 * </pre>
 * <p>
 * The test:
 * </p>
 * <pre>
    &#64;Test
    public void test() {
        final Foo foo = Foo.builder()
            .setBar1("value1")
            .setBar2("value2")
            .setBar3("value3")
            .get();
        assertEquals("value1", foo.getBar1());
        assertEquals("value2", foo.getBar2());
        assertEquals("value3", foo.getBar3());
    }
 * </pre>
 *
 * @param <T> the type of instances to build.
 * @param <B> the type of builder subclass.
 * @since 2.12.0
 */
public abstract class AbstractSupplier<T, B extends AbstractSupplier<T, B>> implements IOSupplier<T> {

    /**
     * Constructs a new instance for subclasses.
     */
    public AbstractSupplier() {
        // empty
    }

    /**
     * Returns this instance typed as the subclass type {@code B}.
     * <p>
     * This is the same as the expression:
     * </p>
     * <pre>
     * (B) this
     * </pre>
     *
     * @return this instance typed as the subclass type {@code B}.
     */
    @SuppressWarnings("unchecked")
    protected B asThis() {
        return (B) this;
    }

}
