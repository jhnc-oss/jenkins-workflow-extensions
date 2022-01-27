/*
 * MIT License
 *
 * Copyright (c) 2021-2022 jhnc-oss
 * Copyright (c) 2004-2019, Sun Microsystems, Inc., Kohsuke Kawaguchi, and other Jenkins contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.jhnc.jenkins.plugins.test;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * JUnit 5 extension providing {@link JenkinsRule} integration.
 *
 * @see EnableJenkins
 */
class JenkinsExtension implements ParameterResolver, AfterEachCallback {

    private static final String KEY = "jenkins-instance";
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(JenkinsExtension.class);

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        final JenkinsRule rule = context.getStore(NAMESPACE).remove(KEY, JenkinsRule.class);
        if (rule == null) {
            return;
        }
        rule.after();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(JenkinsRule.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final JenkinsRule rule = extensionContext.getStore(NAMESPACE).getOrComputeIfAbsent(KEY, key
                -> new JUnit5JenkinsRule(parameterContext, extensionContext), JenkinsRule.class);

        try {
            rule.before();
            return rule;
        } catch (Throwable t) {
            throw new ParameterResolutionException(t.getMessage(), t);
        }
    }
}
