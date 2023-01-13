/*
 * MIT License
 *
 * Copyright (c) 2021-2023 jhnc-oss
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
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import java.util.Date;
import java.util.Objects;

public class ProjectBlockedProperty extends AbstractFolderProperty<WorkflowMultiBranchProject> {
    private String message;
    private Date timestamp;
    private String user;

    public ProjectBlockedProperty(@NonNull String message, @NonNull String user) {
        this.message = message;
        this.timestamp = new Date();
        this.user = user;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    @NonNull
    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    @NonNull
    public String getUser() {
        return user;
    }

    @NonNull
    protected Object readResolve() {
        message = Objects.requireNonNullElse(message, "");
        timestamp = Objects.requireNonNullElse(timestamp, new Date(0));
        user = Objects.requireNonNullElse(user, "");
        return this;
    }


    @Extension
    public static class DescriptorImpl extends AbstractFolderPropertyDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.ProjectBlockedProperty_displayName();
        }
    }

}
