package com.github.jenkins.lastchanges;

import com.github.jenkins.lastchanges.model.LastChangesBuild;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;
import jenkins.branch.MultiBranchProject;
import jenkins.model.TransientActionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LastChangesProjectAction extends LastChangesBaseAction implements ProminentProjectAction {

    private final transient Job<?, ?> job;
    private final List<LastChangesBuild> lastChangesBuilds;
    private final String jobName;

    public LastChangesProjectAction(Job<?, ?> job, List<LastChangesBuild> lastChangesBuilds) {
        this.job = job;
        this.lastChangesBuilds = lastChangesBuilds;
        this.jobName = job.getName();
    }

    public String job() {
        return jobName;
    }

    public Job<?, ?> getProject() {
        return job;
    }

    @Override
    protected String getTitle() {
        return this.job.getDisplayName();
    }

    public List<LastChangesBuild> getLastChangesBuilds() {
        return lastChangesBuilds;
    }
    
    @Extension
    public static class LastChangesActionFactory extends TransientActionFactory<Job<?, ?>> {

        private Boolean isMultiBranchOnClasspath;

        @Override
        public Collection<? extends Action> createFor(Job<?, ?> j) {
            //Dont' run for multibranch projects
            if (isMultiBranch(j)) {
                return Collections.emptyList();
            }
            List<LastChangesBuild> lastChangesBuilds = new ArrayList<>();

            //collects the list of builds that published last changes to show on the last changes history 
            if (j.getBuilds() != null && !j.getBuilds().isEmpty()) {
                for (Run<?, ?> build : j.getBuilds()) {
                    if (build.getAction(LastChangesBuildAction.class) != null) {
                        lastChangesBuilds.add(new LastChangesBuild(build.getNumber(), build.getTime()));
                    }
                }
            }
            if(lastChangesBuilds.isEmpty()) {
                return Collections.emptyList();
            }
            return Collections.singleton(new LastChangesProjectAction(j, lastChangesBuilds));
        }

        private boolean isMultiBranch(Job<?, ?> j) {
            return isMultiBranchPresentOnClasspath() && j.getParent() instanceof MultiBranchProject;
        }

        private boolean isMultiBranchPresentOnClasspath() {
            if(isMultiBranchOnClasspath != null) {
                return isMultiBranchOnClasspath;
            }
            try {
                Class.forName("jenkins.branch.MultiBranchProject");
                isMultiBranchOnClasspath = true;
            } catch (ClassNotFoundException cnf) {
                isMultiBranchOnClasspath = false;
            }
            return isMultiBranchOnClasspath;
        }

        @Override
        public Class type() {
            return Job.class;
        }
    }

}
