package org.jenkinsci.plugins.releaseInfoCapture;

import java.util.ArrayList;
import java.util.List;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;

public class ReleaseInfoCaptureProjectAction implements Action {

	private AbstractProject<?, ?> project;

	ReleaseInfoCaptureProjectAction(final AbstractProject<?, ?> project) {
		// TODO Auto-generated constructor stub
		this.project = project;
	}

	@Override
	public String getIconFileName() {
		// TODO Auto-generated method stub
		return "/plugin/releaseInfoCapture/img/project_icon.png";
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "Release Info Captured Project Action";
	}

	@Override
	public String getUrlName() {
		// TODO Auto-generated method stub
		return "ReleaseInfoCapturedPA";
	}
	
	public AbstractProject<?, ?> getProject() {
        return this.project;
    }

    public String getProjectName() {
        return this.project.getName();
    }

    public List<String> getProjectMessages() {
        List<String> projectMessages = new ArrayList<String>();
        List<? extends AbstractBuild<?, ?>> builds = project.getBuilds();
        String projectMessage="";
        final Class<ReleaseInfoCaptureBuildAction> buildClass = ReleaseInfoCaptureBuildAction.class;

        for (AbstractBuild<?, ?> currentBuild : builds) {
            projectMessage = "Build #"+currentBuild.getAction(buildClass).getBuildNumber()
                    +": "+currentBuild.getAction(buildClass).getMessage();
            projectMessages.add(projectMessage);
        }
        return projectMessages;
    }

}
