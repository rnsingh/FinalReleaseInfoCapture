package org.jenkinsci.plugins.releaseInfoCapture;

import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Alim Azad
 */
public class ReleaseInfoCapturePublisher extends Recorder {

    private final String name;

    @DataBoundConstructor
    public ReleaseInfoCapturePublisher(String name) {
        this.name = name;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {

        String message;
        String jobName;
        String CHNG;
        String buildTag;
        String buildRevision;
        String buildURL;
        
        EnvVars envVars = new EnvVars();
        envVars = build.getEnvironment(listener);
        jobName = envVars.get("JOB_NAME");
        
        // Get git/svn commit revision
        if(envVars.containsKey("GIT_COMMIT"))
        {
        	buildRevision = envVars.get("GIT_COMMIT");
        }
        else
        {
        	buildRevision = envVars.get("SVN_REVISION");
        }
        
        // Get git branch/svn tag or branch details
        if(envVars.containsKey("GIT_BRANCH"))
        {
        	buildTag = envVars.get("GIT_BRANCH");
        }
        else
        {
        	if(envVars.containsKey("Tag"))
        	{
        		buildTag = envVars.get("Tag");
        	}
        	else
        	{
        		buildTag = envVars.get("branch_name");
        	}
        }
        
        if(envVars.containsKey("GIT_URL"))
        {
        	buildURL = envVars.get("GIT_URL");
        }
        else
        {
        	buildURL = envVars.get("SVN_URL");
        }

        CHNG = envVars.get("CHNG");
        message="Hello, " + name + "!";
        
        listener.getLogger().println("CHNG          	: " + CHNG);
        listener.getLogger().println("JOB NAME  		: " + jobName);
        listener.getLogger().println("BUILD REVISION  	: " + buildRevision);
        listener.getLogger().println("BRANCH/TAG	 	: " + buildTag);
        listener.getLogger().println("BUILD URL	 		: " + buildURL);
        listener.getLogger().println("APPLICATION NAME 	: " + name);
        
        System.out.println("CHNG          		: " + CHNG);
        System.out.println("JOB NAME  			: " + jobName);
        System.out.println("BUILD REVISION  	: " + buildRevision);
        System.out.println("BRANCH/TAG	 		: " + buildTag);
        System.out.println("BUILD URL	 		: " + buildURL);
        System.out.println("APPLICATION NAME 	: " + name);

        ReleaseNotesWikiHttpUpdate relnotes = new ReleaseNotesWikiHttpUpdate();
        try
        {
        	relnotes.updateWiki(name,CHNG,jobName,buildRevision,buildTag,buildURL,listener);
        }catch(Exception e){
        	e.printStackTrace();
        }
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please provide an Application name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Generate Release Notes";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        	formData.getString("name");
            save();
            return super.configure(req, formData);
        }

    }
}
I