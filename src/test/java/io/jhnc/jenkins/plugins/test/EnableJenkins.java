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

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JUnit 5 meta annotation providing {@link org.jvnet.hudson.test.JenkinsRule JenkinsRule} integration.
 *
 * <p>
 * Test methods using the rule extension need to accept it by {@link org.jvnet.hudson.test.JenkinsRule JenkinsRule} parameter; each test case
 * gets a new rule object.
 * <p>
 * Annotating a <em>class</em> provides access for all of its tests. Unrelated test cases can omit the parameter.
 *
 * <blockquote><pre>
 * &#64;EnableJenkins
 * class ExampleJUnit5Test {
 *
 *     &#64;Test
 *     public void example(JenkinsRule r) {
 *         // use 'r' ...
 *     }
 *
 *     &#64;Test
 *     public void exampleNotUsingRule() {
 *         // ...
 *     }
 * }
 * </pre></blockquote>
 * <p>
 * Annotating a <i>method</i> limits access to the method.
 *
 * <blockquote><pre>
 * class ExampleJUnit5Test {
 *
 *     &#64;EnableJenkins
 *     &#64;Test
 *     public void example(JenkinsRule r) {
 *         // use 'r' ...
 *     }
 * }
 * </pre></blockquote>
 *
 * @see JenkinsExtension
 * @see org.junit.jupiter.api.extension.ExtendWith
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(JenkinsExtension.class)
public @interface EnableJenkins {
}
