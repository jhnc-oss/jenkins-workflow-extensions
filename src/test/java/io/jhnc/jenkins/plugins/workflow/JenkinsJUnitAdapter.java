/*
 * MIT License
 *
 * Copyright (c) 2021 jhnc-oss
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

package io.jhnc.jenkins.plugins.workflow;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.runner.Description;
import org.jvnet.hudson.test.JenkinsRecipe;

import java.lang.reflect.Method;
import java.util.Optional;

public class JenkinsJUnitAdapter {
    public static class JUnitJenkinsRule extends org.jvnet.hudson.test.JenkinsRule {
        private final ParameterContext context;

        JUnitJenkinsRule(@NonNull ParameterContext context, @NonNull ExtensionContext extensionContext) {
            this.context = context;
            this.testDescription = Description.createTestDescription(
                    extensionContext.getTestClass().map(Class::getName).orElse(null),
                    extensionContext.getTestMethod().map(Method::getName).orElse(null)
            );
        }

        @Override
        public void recipe() throws Exception {
            final Optional<JenkinsRecipe> jenkinsRecipe = context.findAnnotation(JenkinsRecipe.class);

            if (jenkinsRecipe.isPresent()) {
                @SuppressWarnings("unchecked") final JenkinsRecipe.Runner<JenkinsRecipe> runner =
                        (JenkinsRecipe.Runner<JenkinsRecipe>) jenkinsRecipe.get().value()
                                .getDeclaredConstructor().newInstance();
                recipes.add(runner);
                tearDowns.add(() -> runner.tearDown(this, jenkinsRecipe.get()));
            }
        }
    }

    public static class JenkinsParameterResolver implements ParameterResolver, AfterEachCallback {
        private static final String key = "jenkins-instance";
        private static final ExtensionContext.Namespace ns =
                ExtensionContext.Namespace.create(JenkinsParameterResolver.class);

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
                throws ParameterResolutionException {
            return parameterContext.getParameter().getType().equals(JUnitJenkinsRule.class);
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
                throws ParameterResolutionException {
            final JUnitJenkinsRule instance = extensionContext.getStore(ns)
                    .getOrComputeIfAbsent(key, key
                            -> new JUnitJenkinsRule(parameterContext, extensionContext), JUnitJenkinsRule.class);
            try {
                instance.before();
                return instance;
            } catch (Throwable t) {
                throw new ParameterResolutionException(t.toString());
            }
        }

        @Override
        public void afterEach(ExtensionContext extensionContext) throws Exception {
            final JUnitJenkinsRule rule = extensionContext.getStore(ns).remove(key, JUnitJenkinsRule.class);

            if (rule != null) {
                rule.after();
            }
        }
    }
}
