/*
 * MIT License
 *
 * Copyright (c) 2021-2022 jhnc-oss
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

package io.jhnc.jenkins.plugins.workflow.queue;

import hudson.model.Action;
import hudson.model.FreeStyleProject;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

class BlockPipelineActionFactoryTest {
    @Test
    void actionLimitedToMultiBranchProjects() {
        final BlockPipelineActionFactory factory = new BlockPipelineActionFactory();
        assertThat(factory.type()).isAssignableTo(MultiBranchProject.class);
        assertThat(FreeStyleProject.class.isAssignableFrom(factory.type())).isFalse();
    }

    @Test
    void createForCreatesActionsForProject() {
        final BlockPipelineActionFactory factory = new BlockPipelineActionFactory();
        final WorkflowMultiBranchProject project = mock(WorkflowMultiBranchProject.class);
        final Collection<? extends Action> actions = factory.createFor(project);

        assertThat(actions).hasSize(1);
        assertThat(actions.iterator().next()).isInstanceOf(BlockPipelineAction.class);
    }

}