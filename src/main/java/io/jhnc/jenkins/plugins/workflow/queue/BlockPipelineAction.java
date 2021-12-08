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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.User;
import hudson.security.Permission;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

public class BlockPipelineAction implements Action, StaplerProxy {
    private static final Permission PERMISSION = Item.CONFIGURE;
    private final MultiBranchProject<WorkflowJob, WorkflowRun> project;

    public BlockPipelineAction(MultiBranchProject<WorkflowJob, WorkflowRun> project) {
        this.project = project;
    }


    @CheckForNull
    @Override
    public String getIconFileName() {
        return project.hasPermission(PERMISSION) ? "/plugin/jenkins-workflow-extensions/images/lock.png" : null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Block";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "block";
    }

    @Override
    public Object getTarget() {
        project.checkPermission(PERMISSION);
        return this;
    }

    public boolean isBlocked() {
        return getProjectProperty() != null;
    }

    @SuppressWarnings("unchecked")
    public Collection<? extends Job<?, ?>> getJobs() {
        return (Collection<? extends Job<?, ?>>) project.getAllJobs();
    }

    public boolean isBlocked(@NonNull Job<?, ?> job) {
        return job.getProperty(JobBlockedProperty.class) != null;
    }

    @CheckForNull
    public String getMessage() {
        final ProjectBlockedProperty property = getProjectProperty();
        return property != null ? property.getMessage() : null;
    }

    @CheckForNull
    public Date getTimestamp() {
        final ProjectBlockedProperty property = getProjectProperty();
        return property != null ? property.getTimestamp() : null;
    }

    @CheckForNull
    public String getUserName() {
        final ProjectBlockedProperty property = getProjectProperty();
        return property != null ? property.getUser() : null;
    }

    @RequirePOST
    public HttpResponse doBlockJob(@NonNull StaplerRequest req) throws IOException {
        final String jobName = req.getParameter("job");
        final WorkflowJob job = project.getJob(jobName);

        if (job == null) {
            return FormValidation.error("No Job '" + jobName + "' available");
        }

        addBlockPropertyToJob(job);
        return FormApply.success(".");
    }

    @RequirePOST
    public HttpResponse doUnblockJob(@NonNull StaplerRequest req) throws IOException {
        final String jobName = req.getParameter("job");
        final WorkflowJob job = project.getJob(jobName);

        if (job == null) {
            return FormValidation.error("No Job '" + jobName + "' available");
        }

        removeBlockPropertyFromJob(job);
        return FormApply.success(".");
    }

    @RequirePOST
    public HttpResponse doBlock(@NonNull StaplerRequest req) throws IOException, ServletException {
        addBlockProperty(req.getSubmittedForm().getString("message").trim());
        return FormApply.success(".");
    }

    @RequirePOST
    public HttpResponse doUnblock(@NonNull StaplerRequest req) throws IOException {
        removeBlockProperty();
        return FormApply.success(".");
    }

    protected void addBlockProperty(@NonNull String message) throws IOException {
        project.getProperties().replace(new ProjectBlockedProperty(message, getCurrentUser().getFullName()));

        for (final Job<?, ?> job : project.getAllJobs()) {
            addBlockPropertyToJob(job);
        }
    }

    protected void removeBlockProperty() throws IOException {
        project.getProperties().remove(ProjectBlockedProperty.class);

        for (final Job<?, ?> job : project.getAllJobs()) {
            removeBlockPropertyFromJob(job);
        }
    }

    protected void addBlockPropertyToJob(@NonNull Job<?, ?> job) throws IOException {
        if (job.getProperty(JobBlockedProperty.class) == null) {
            job.addProperty(new JobBlockedProperty());
        }
    }

    protected void removeBlockPropertyFromJob(@NonNull Job<?, ?> job) throws IOException {
        job.removeProperty(JobBlockedProperty.class);
    }

    @NonNull
    protected User getCurrentUser() {
        final User current = User.current();
        return current == null ? User.getUnknown() : current;
    }

    @CheckForNull
    private ProjectBlockedProperty getProjectProperty() {
        return project.getProperties().get(ProjectBlockedProperty.class);
    }

}
