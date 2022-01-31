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

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.util.DescribableList;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockPipelineQueueDispatcherTest {

    @Mock
    WorkflowMultiBranchProject project;


    @Test
    void unrelatedItemsAreIgnored() {
        final BlockPipelineQueueDispatcher dispatcher = new BlockPipelineQueueDispatcher();
        final Queue.Item item = mock(Queue.Item.class);
        assertThat(dispatcher.canRun(item)).isNull();
    }

    @Test
    void unrelatedJobTypeIsIgnored() {
        final BlockPipelineQueueDispatcher dispatcher = new BlockPipelineQueueDispatcher();
        final FreeStyleProject job = mock(FreeStyleProject.class);

        assertThat(dispatcher.canRun(createItem(job))).isNull();
    }

    @Test
    void unblockedJobIsExecuted() {
        final BlockPipelineQueueDispatcher dispatcher = new BlockPipelineQueueDispatcher();
        final AbstractProject<?, ?> job = mock(AbstractProject.class);

        assertThat(dispatcher.canRun(createItem(job))).isNull();
    }

    @Test
    void blockedJobIsNotExecuted() {
        final BlockPipelineQueueDispatcher dispatcher = new BlockPipelineQueueDispatcher();
        when(project.getProperties()).thenReturn(new DescribableList<>(project, Collections.emptyList()));
        final AbstractProject<?, ?> job = mock(AbstractProject.class);
        when(job.getParent()).thenAnswer(x -> project);
        when(job.getProperty(JobBlockedProperty.class)).thenReturn(new JobBlockedProperty());
        final CauseOfBlockage cause = dispatcher.canRun(createItem(job));

        assertThat(cause).isNotNull();
        assertThat(cause).isInstanceOf(BlockPipelineQueueDispatcher.JobBlockedCause.class);
        assertThat(cause.getShortDescription()).isNotEmpty();
    }

    @Test
    void blockedJobIsNotExecutedIfParentIsUnblocked() {
        final BlockPipelineQueueDispatcher dispatcher = new BlockPipelineQueueDispatcher();
        when(project.getProperties()).thenReturn(new DescribableList<>(project, Collections.emptyList()));
        final AbstractProject<?, ?> job = mock(AbstractProject.class);
        when(job.getProperty(JobBlockedProperty.class)).thenReturn(new JobBlockedProperty());
        when(job.getParent()).thenAnswer(x -> project);
        final CauseOfBlockage cause = dispatcher.canRun(createItem(job));

        assertThat(cause).isNotNull();
        assertThat(cause).isInstanceOf(BlockPipelineQueueDispatcher.JobBlockedCause.class);
        assertThat(cause.getShortDescription()).isNotEmpty();
    }

    @Test
    void unblockedProjectIsExecuted() {
        final BlockPipelineQueueDispatcher dispatcher = new BlockPipelineQueueDispatcher();
        when(project.getProperties()).thenReturn(new DescribableList<>(project, Collections.emptyList()));
        final WorkflowJob job = new WorkflowJob(project, "x");
        final CauseOfBlockage cause = dispatcher.canRun(createItem(job));

        assertThat(cause).isNull();
    }

    @Test
    void blockedProjectIsNotExecuted() {
        final BlockPipelineQueueDispatcher dispatcher = new BlockPipelineQueueDispatcher();
        when(project.getProperties()).thenReturn(new DescribableList<>(project, Collections.singleton(new ProjectBlockedProperty("", "user"))));
        final WorkflowJob job = new WorkflowJob(project, "x");
        final CauseOfBlockage cause = dispatcher.canRun(createItem(job));

        assertThat(cause).isNotNull();
        assertThat(cause).isInstanceOf(BlockPipelineQueueDispatcher.JobBlockedCause.class);
        assertThat(cause.getShortDescription()).isNotEmpty();
    }

    @Test
    void blockedProjectWithCustomMessage() {
        final BlockPipelineQueueDispatcher dispatcher = new BlockPipelineQueueDispatcher();
        final ProjectBlockedProperty property = new ProjectBlockedProperty("a custom message", "user");
        when(project.getProperties()).thenReturn(new DescribableList<>(project, Collections.singleton(property)));
        final WorkflowJob job = new WorkflowJob(project, "x");
        final CauseOfBlockage cause = dispatcher.canRun(createItem(job));

        assertThat(cause).isNotNull();
        assertThat(cause).isInstanceOf(BlockPipelineQueueDispatcher.JobBlockedCause.class);
        assertThat(cause.getShortDescription()).contains("a custom message");
    }

    private Queue.Item createItem(Queue.Task task) {
        return new Queue.WaitingItem(null, task, Collections.emptyList());
    }

}