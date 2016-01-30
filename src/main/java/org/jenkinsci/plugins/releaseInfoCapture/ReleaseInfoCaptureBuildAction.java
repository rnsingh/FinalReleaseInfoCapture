package org.jenkinsci.plugins.releaseInfoCapture;

import hudson.model.AbstractBuild;
import hudson.model.Action;

public class ReleaseInfoCaptureBuildAction implements Action{

	private String message;
	private AbstractBuild<?, ?> build;

	public ReleaseInfoCaptureBuildAction(String message, AbstractBuild build) {
		// TODO Auto-generated constructor stub
        this.message = message;
        this.build = build;
	}

	@Override
	public String getIconFileName() {
		// TODO Auto-generated method stub
		return "/plugin/releaseInfoCapture/img/build-goals.png";
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "Release Info Captured Build Page";
	}

	@Override
	public String getUrlName() {
		// TODO Auto-generated method stub
		return "ReleaseInfocapturedBA";
	}

	public String getMessage() {
        return this.message;
    }

    public int getBuildNumber() {
        return this.build.number;
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

}
