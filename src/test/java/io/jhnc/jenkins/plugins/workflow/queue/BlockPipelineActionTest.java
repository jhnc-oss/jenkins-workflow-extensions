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

package io.jhnc.jenkins.plugins.workflow.queue;

import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import hudson.model.Item;
import hudson.model.Job;
import hudson.security.Permission;
import hudson.util.DescribableList;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockPipelineActionTest {
    @Mock
    MultiBranchProject<WorkflowJob, WorkflowRun> project;
    @Mock
    StaplerRequest req;

    @Test
    void visibleIfPermissionGranted() {
        when(project.hasPermission(Item.CONFIGURE)).thenReturn(true);
        final BlockPipelineAction action = new BlockPipelineAction(project);
        assertThat(action.getIconFileName()).isNotNull();
    }

    @Test
    void notVisibleIfPermissionDenied() {
        when(project.hasPermission(Item.CONFIGURE)).thenReturn(false);
        final BlockPipelineAction action = new BlockPipelineAction(project);
        assertThat(action.getIconFileName()).isNull();
    }

    @Test
    void hasStablePath() {
        final BlockPipelineAction action = new BlockPipelineAction(project);
        assertThat(action.getUrlName()).isEqualTo("block");
    }

    @Test
    void checksPermission() {
        final BlockPipelineAction action = new BlockPipelineAction(project);
        assertThat(action.getTarget()).isNotNull();
        verify(project).checkPermission(Item.CONFIGURE);
    }

    @Test
    void throwsOnAccessDenied() {
        doThrow(new AccessDeniedException("expected")).when(project).checkPermission(any(Permission.class));
        final BlockPipelineAction action = new BlockPipelineAction(project);
        assertThrows(AccessDeniedException.class, action::getTarget);
    }

    @Test
    void isBlockedReturnsProjectState() {
        when(project.getProperties()).thenReturn(folderProperty());
        final BlockPipelineAction action = new BlockPipelineAction(project);
        assertThat(action.isBlocked()).isTrue();
    }

    @Test
    void getJobsReturnsAllJobsOfTheProject() {
        final Job<?, ?> job0 = mock(Job.class);
        final Job<?, ?> job1 = mock(Job.class);
        when(project.getAllJobs()).thenAnswer(x -> asList(job0, job1));
        final BlockPipelineAction action = new BlockPipelineAction(project);
        assertThat(action.getJobs()).containsExactly(job0, job1);
    }

    @Test
    void blockDoesNothingIfNoJobAvailable() throws IOException {
        when(project.getAllJobs()).thenReturn(Collections.emptyList());
        when(project.getProperties()).thenReturn(emptyFolderProperty());
        final BlockPipelineAction action = new BlockPipelineAction(project);
        final HttpResponse resp = action.doBlock(req);
        assertThat(resp).isNotNull();
    }

    @Test
    void blockAddsProperty() throws IOException {
        final Job<?, ?> job = mock(Job.class);
        when(project.getAllJobs()).thenAnswer(x -> asList(job));
        when(project.getProperties()).thenReturn(emptyFolderProperty());
        final BlockPipelineAction action = new BlockPipelineAction(project);
        final HttpResponse resp = action.doBlock(req);
        assertThat(resp).isNotNull();

        verify(job).addProperty(any(JobBlockedProperty.class));
        verify(project).addProperty(any(ProjectBlockedProperty.class));
    }

    @Test
    void blockAddsPropertyOnlyOnce() throws IOException {
        final Job<?, ?> job = mock(Job.class);
        when(project.getAllJobs()).thenAnswer(x -> asList(job));
        when(project.getProperties()).thenReturn(emptyFolderProperty()).thenReturn(folderProperty());
        when(job.getProperty(JobBlockedProperty.class)).thenReturn(null, new JobBlockedProperty());
        final BlockPipelineAction action = new BlockPipelineAction(project);
        action.doBlock(req);
        action.doBlock(req);
        action.doBlock(req);

        verify(job).addProperty(any(JobBlockedProperty.class));
        verify(project).addProperty(any(ProjectBlockedProperty.class));
    }

    @Test
    void blockAddsPropertyToMultipleJobs() throws IOException {
        final Job<?, ?> job0 = mock(Job.class);
        final Job<?, ?> job1 = mock(Job.class);
        final Job<?, ?> job2 = mock(Job.class);
        when(project.getAllJobs()).thenAnswer(x -> asList(job0, job1, job2));
        when(project.getProperties()).thenReturn(emptyFolderProperty());
        final BlockPipelineAction action = new BlockPipelineAction(project);
        final HttpResponse resp = action.doBlock(req);
        assertThat(resp).isNotNull();

        verify(job0).addProperty(any(JobBlockedProperty.class));
        verify(job1).addProperty(any(JobBlockedProperty.class));
        verify(job2).addProperty(any(JobBlockedProperty.class));
        verify(project).addProperty(any(ProjectBlockedProperty.class));
    }

    @Test
    void unblockDoesNothingIfNotBlocked() throws IOException {
        final DescribableList<AbstractFolderProperty<?>, AbstractFolderPropertyDescriptor> properties = emptyFolderProperty();
        when(project.getAllJobs()).thenReturn(Collections.emptyList());
        when(project.getProperties()).thenReturn(properties);
        final BlockPipelineAction action = new BlockPipelineAction(project);
        final HttpResponse resp = action.doUnblock(req);

        assertThat(resp).isNotNull();
        assertThat(properties).isEmpty();
    }

    @Test
    void unblockRemovesPropertyFromProjectIfNoJobAvailable() throws IOException {
        final DescribableList<AbstractFolderProperty<?>, AbstractFolderPropertyDescriptor> properties = folderProperty();
        when(project.getAllJobs()).thenReturn(Collections.emptyList());
        when(project.getProperties()).thenReturn(properties);
        final BlockPipelineAction action = new BlockPipelineAction(project);
        final HttpResponse resp = action.doUnblock(req);

        assertThat(resp).isNotNull();
        assertThat(properties).isEmpty();
    }

    @Test
    void unblockRemovesProperty() throws IOException {
        final Job<?, ?> job = mock(Job.class);
        final DescribableList<AbstractFolderProperty<?>, AbstractFolderPropertyDescriptor> properties = folderProperty();
        when(project.getAllJobs()).thenAnswer(x -> asList(job));
        when(project.getProperties()).thenReturn(properties);
        final BlockPipelineAction action = new BlockPipelineAction(project);
        final HttpResponse resp = action.doUnblock(req);

        assertThat(resp).isNotNull();
        verify(job).removeProperty(JobBlockedProperty.class);
        assertThat(properties).isEmpty();
    }

    @Test
    void unblockRemovesPropertyFromMultipleJobs() throws IOException {
        final Job<?, ?> job0 = mock(Job.class);
        final Job<?, ?> job1 = mock(Job.class);
        final Job<?, ?> job2 = mock(Job.class);
        final DescribableList<AbstractFolderProperty<?>, AbstractFolderPropertyDescriptor> properties = folderProperty();
        when(project.getAllJobs()).thenAnswer(x -> asList(job0, job1, job2));
        when(project.getProperties()).thenReturn(properties);
        final BlockPipelineAction action = new BlockPipelineAction(project);
        final HttpResponse resp = action.doUnblock(req);
        assertThat(resp).isNotNull();

        verify(job0).removeProperty(JobBlockedProperty.class);
        verify(job1).removeProperty(JobBlockedProperty.class);
        verify(job2).removeProperty(JobBlockedProperty.class);
        assertThat(properties).isEmpty();
    }

    @Test
    void isJobBlockedReturnsBlockStateOfJob() {
        final Job<?, ?> blocked = mock(Job.class);
        final Job<?, ?> unblocked = mock(Job.class);
        when(blocked.getProperty(JobBlockedProperty.class)).thenReturn(new JobBlockedProperty());
        final BlockPipelineAction action = new BlockPipelineAction(project);

        assertThat(action.isBlocked(blocked)).isTrue();
        assertThat(action.isBlocked(unblocked)).isFalse();
    }

    @Test
    void blockJobBlockJobsIfAvailable() throws IOException {
        final WorkflowJob job = new WorkflowJob(project, "test-0");
        when(req.getParameter("job")).thenReturn("test-0");
        when(project.getJob("test-0")).thenReturn(job);
        final BlockPipelineAction actionSpy = spy(new BlockPipelineAction(project));
        doNothing().when(actionSpy).addBlockPropertyToJob(any());

        final HttpResponse resp = actionSpy.doBlockJob(req);
        assertThat(resp).isNotNull();
        verify(actionSpy).addBlockPropertyToJob(job);
    }

    @Test
    void blockJobDoesNotBlockJobIfNotAvailable() throws IOException {
        when(req.getParameter("job")).thenReturn("not-existing-job");
        final BlockPipelineAction actionSpy = spy(new BlockPipelineAction(project));

        final HttpResponse resp = actionSpy.doBlockJob(req);
        assertThat(resp).isNotNull();
    }

    @Test
    void blockJobIsSafeToMissingParameter() throws IOException {
        final BlockPipelineAction actionSpy = spy(new BlockPipelineAction(project));

        final HttpResponse resp = actionSpy.doBlockJob(req);
        assertThat(resp).isNotNull();
    }

    @Test
    void unblockJobUnblockJobsIfAvailable() throws IOException {
        final WorkflowJob job = new WorkflowJob(project, "test-0");
        when(req.getParameter("job")).thenReturn("test-0");
        when(project.getJob("test-0")).thenReturn(job);
        final BlockPipelineAction actionSpy = spy(new BlockPipelineAction(project));

        final HttpResponse resp = actionSpy.doUnblockJob(req);
        assertThat(resp).isNotNull();
        verify(actionSpy).removeBlockPropertyFromJob(job);
    }

    @Test
    void unblockJobDoesNotUnblockJobIfNotAvailable() throws IOException {
        when(req.getParameter("job")).thenReturn("not-existing-job");
        final BlockPipelineAction actionSpy = spy(new BlockPipelineAction(project));

        final HttpResponse resp = actionSpy.doUnblockJob(req);
        assertThat(resp).isNotNull();
    }

    @Test
    void unblockJobIsSafeToMissingParameter() throws IOException {
        final BlockPipelineAction actionSpy = spy(new BlockPipelineAction(project));

        final HttpResponse resp = actionSpy.doUnblockJob(req);
        assertThat(resp).isNotNull();
    }


    private DescribableList<AbstractFolderProperty<?>, AbstractFolderPropertyDescriptor> folderProperty() {
        return new DescribableList<>(project, Collections.singleton(new ProjectBlockedProperty()));
    }

    private DescribableList<AbstractFolderProperty<?>, AbstractFolderPropertyDescriptor> emptyFolderProperty() {
        return new DescribableList<>(project, Collections.emptyList());

    }

    @SafeVarargs
    private final <T> List<T> asList(T... elements) {
        return Arrays.asList(elements);
    }
}