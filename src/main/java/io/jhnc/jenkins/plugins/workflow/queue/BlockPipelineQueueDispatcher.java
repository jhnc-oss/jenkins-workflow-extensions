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
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

@Extension
public class BlockPipelineQueueDispatcher extends QueueTaskDispatcher {
    @CheckForNull
    @Override
    public CauseOfBlockage canRun(Queue.Item item) {
        if (item.task instanceof Job) {
            final Job<?, ?> job = (Job<?, ?>) item.task;

            if (isBlocked(job)) {
                return new JobBlockedCause();
            }
        }
        return super.canRun(item);
    }

    private boolean isBlocked(@NonNull Job<?, ?> job) {
        if (job.getParent() instanceof WorkflowMultiBranchProject) {
            final WorkflowMultiBranchProject parent = (WorkflowMultiBranchProject) job.getParent();

            if (parent.getProperties().get(ProjectBlockedProperty.class) != null) {
                return true;
            }
        }
        return job.getProperty(JobBlockedProperty.class) != null;
    }


    public static class JobBlockedCause extends CauseOfBlockage {
        @Override
        public String getShortDescription() {
            return Messages.BlockPipelineQueueDispatcher_shortDescription();
        }
    }
}
