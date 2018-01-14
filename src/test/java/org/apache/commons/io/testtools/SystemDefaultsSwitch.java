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
package org.apache.commons.io.testtools;

import java.util.Locale;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test Rule used with {@link SystemDefaults} annotation that sets and restores the system default Locale.
 *
 * <p>
 * Set up tests to use alternate system default Locale by creating an instance of this rule
 * and annotating the test method with {@link SystemDefaults}
 * </p>
 *
 * <pre>
 * public class SystemDefaultsDependentTest {
 *
 *     {@literal @}Rule
 *     public SystemDefaultsSwitch locale = new SystemDefaultsSwitch();
 *
 *     {@literal @}Test
 *     {@literal @}SystemDefaults(local="zh_CN")
 *     public void testWithSimplifiedChinaDefaultLocale() {
 *         // Locale.getDefault() will return Locale.CHINA until the end of this test method
 *     }
 * }
 * </pre>
 */
public class SystemDefaultsSwitch implements TestRule {

    @Override
    public Statement apply(final Statement stmt, final Description description) {
        final SystemDefaults defaults = description.getAnnotation(SystemDefaults.class);
        if (defaults == null) {
            return stmt;
        }
        return applyLocale(defaults, stmt);
    }

    private Statement applyLocale(final SystemDefaults defaults, final Statement stmt) {
        if (defaults.locale().isEmpty()) {
            return stmt;
        }

        final Locale newLocale = Locale.forLanguageTag(defaults.locale());

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final Locale save = Locale.getDefault();
                try {
                    Locale.setDefault(newLocale);
                    stmt.evaluate();
                } finally {
                    Locale.setDefault(save);
                }
            }
        };
    }

}
