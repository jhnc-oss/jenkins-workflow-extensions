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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import hudson.scm.NullSCM;
import io.jhnc.jenkins.plugins.workflow.JenkinsJUnitAdapter;
import jenkins.branch.Branch;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMHead;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IT")
@ExtendWith(JenkinsJUnitAdapter.JenkinsParameterResolver.class)
public class BlockPipelineActionAccessTest {
    @Test
    void blockAndUnblockFailWithoutPost(JenkinsJUnitAdapter.JUnitJenkinsRule r) throws IOException {
        final MultiBranchProject<WorkflowJob, WorkflowRun> project = createMultiBranch(r, "project-0");
        assertFailsWithoutPost(r, project.getUrl() + "block/block");
        assertFailsWithoutPost(r, project.getUrl() + "block/unblock");
    }

    @Test
    void blockAndUnblockOfJobFailWithoutPost(JenkinsJUnitAdapter.JUnitJenkinsRule r) throws IOException {
        final MultiBranchProject<WorkflowJob, WorkflowRun> project = createMultiBranch(r, "project-1");
        final Branch branch = new Branch("ignored", new SCMHead("master"), new NullSCM(), Collections.emptyList());
        project.getProjectFactory().newInstance(branch);
        assertFailsWithoutPost(r, project.getUrl() + "block/blockJob?job=master");
        assertFailsWithoutPost(r, project.getUrl() + "block/unblockJob?job=master");
    }

    private WorkflowMultiBranchProject createMultiBranch(JenkinsJUnitAdapter.JUnitJenkinsRule j, String name) throws IOException {
        return j.jenkins.createProject(WorkflowMultiBranchProject.class, name);
    }

    private void assertFailsWithoutPost(JenkinsJUnitAdapter.JUnitJenkinsRule r, String url) {
        final JenkinsJUnitAdapter.JUnitJenkinsRule.WebClient webClient = r.createWebClient();
        final FailingHttpStatusCodeException e = assertThrows(FailingHttpStatusCodeException.class, () -> webClient.goTo(url));
        assertThat(e.getStatusCode()).isEqualTo(405);
    }

}
