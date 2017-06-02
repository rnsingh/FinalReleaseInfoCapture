package org.jenkinsci.plugins.releaseInfoCapture;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import hudson.model.BuildListener;
import hudson.model.Result;

public class ReleaseNotesWikiHttpUpdate {

	private static final String BASE_URL = "https://rsingh.atlassian.net";
	private static final String USERNAME = "rohitnarayansingh@gmail.com";
	private static final String PASSWORD = "Singh8450";
	private static final String ENCODING = "utf-8";
 
	private static String postContentRestUrl() throws UnsupportedEncodingException {
		// RestApi call for authorization and posting a new page within the required space
		return String.format("%s/rest/api/content/?os_authType=basic&os_username=%s&os_password=%s",BASE_URL,URLEncoder.encode(USERNAME, ENCODING),URLEncoder.encode(PASSWORD, ENCODING));
 }
	
	private static String getContentRestUrl(final String title,final String spaceKey) throws UnsupportedEncodingException {
		// RestApi call for authorization and search page by title and spaceKey
		return String.format("%s/rest/api/content?title=%s&spaceKey=%s&os_authType=basic&os_username=%s&os_password=%s",BASE_URL,title,spaceKey,URLEncoder.encode(USERNAME, ENCODING),URLEncoder.encode(PASSWORD, ENCODING));
 }
	
	private static String putContentRestUrl(final Long contentId,final String[] expansions) throws UnsupportedEncodingException {
		// RestApi call for authorization and post new contents for the specific page id
		final String expand = URLEncoder.encode(StringUtils.join(expansions, ","), ENCODING);
		return String.format("%s/rest/api/content/%s?expand=%s&os_authType=basic&os_username=%s&os_password=%s",BASE_URL, contentId, expand,URLEncoder.encode(USERNAME, ENCODING),URLEncoder.encode(PASSWORD, ENCODING));
 }
	public Integer updateWiki(String appname, String CHNG, String jobName, String buildTimer, String buildRevision, String buildTag, String buildURL, BuildListener listener) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		
		DefaultHttpClient client = new DefaultHttpClient();
		String pageObj = null;
		HttpEntity pageEntity = null;
		HttpEntity getPageEntity = null;
		String testpage = appname; //pass through the calling class
		Integer size;
		String tableoutput = "";
		String pattern1 = "<tbody>";
		String pattern2 = "</tbody>";
		String get1pageObj = null;
		HttpEntity get1pageEntity = null;
		Integer result = 0;
		boolean b = true;
		
		String text = "<body>"
				+ "<h1>" + appname + " Release Info</h1>"
				+ "<table>"
				+ "<tr>"
				+ "<th>CHNG</th>"
				+ "<th>JOB NAME</th>"
				+ "<th>BUILD NO_TIMESTAMP</th>"
				+ "<th>REVISION</th>"
				+ "<th>TAG/BRANCH</th>"
				+ "<th>SOURCE</th>"
				+ "<th>RELEASE DATE</th>"
				+ "</tr>"
				+ "</table>" + "</body>" + "<p> </p>"
		         + "<p> </p>" + "<p>"
		         + "<ac:structured-macro ac:name='gallery'>"
		         + "<ac:parameter ac:name='columns'>1</ac:parameter>"
		         + "</ac:structured-macro>" + "</p>";

		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = new Date();
		String reportDate = dateFormat.format(date);
		System.out.println("Date : " + reportDate);
		listener.getLogger().println("INFO : Release Date - " + reportDate);
		

		GenerateParentPage gpp = new GenerateParentPage();
		Long parentid = gpp.GetParentPage(listener);
		
		//String json = "{\"type\":\"page\",\"title\":\"" + testpage + "\",\"space\":{\"key\":\"ds\"},\"body\":{\"storage\":{\"value\":\"" + text + "\",\"representation\":\"storage\"}}}";
		String json = "{\"type\":\"page\",\"ancestors\":[{\"type\":\"page\",\"id\":" + parentid + "}],\"title\":\"" + testpage + "\",\"space\":{\"key\":\"ds\"},\"body\":{\"storage\":{\"value\":\"" + text + "\",\"representation\":\"storage\"}}}";
		
		// Find Page and get value of size. If size equals 0 - Page doesn't exist. If size not equals 0 - Page Exist
		System.out.println("INFO : Checking Page Existence");
		listener.getLogger().println("INFO : Checking Page Existence");
		String testpagecoder = URLEncoder.encode(testpage, ENCODING);
		
		HttpGet getPageRequest = new HttpGet(getContentRestUrl(testpagecoder,"ds")); // Search in space "ds"
		HttpResponse getPageResponse = client.execute(getPageRequest);
		getPageEntity = getPageResponse.getEntity();
		String getPageObj = IOUtils.toString(getPageEntity.getContent());
	
		System.out.println("INFO : Get Page Details Request Returned Status - " + getPageResponse.getStatusLine().toString());
		listener.getLogger().println("INFO : Get Page Details Request Returned Status - " + getPageResponse.getStatusLine().toString());
	
		JSONObject getbodyPage = new JSONObject(getPageObj);
		size = getbodyPage.getInt("size");
		System.out.println("INFO : Size - " + getbodyPage.getInt("size"));
		listener.getLogger().println("INFO : Size - " + getbodyPage.getInt("size"));
		
		if ( size == 0)
		{
			System.out.println("INFO : Status Code 404");
			listener.getLogger().println("INFO : Status Code 404");
			System.out.println("INFO : Page Not Found");
			listener.getLogger().println("INFO : Page Not Found");
			try {
				// Create a new Page within space "ds"
				System.out.println("INFO : Generating Page " + testpage);
				listener.getLogger().println("INFO : Generating Page " + testpage);
			
				HttpPost postRequest = new HttpPost(postContentRestUrl());
				String url = URLEncoder.encode(testpage, ENCODING);
				StringEntity input = new StringEntity(json);
				postRequest.setHeader("Accept", "application/json");
				postRequest.setHeader("Content-type", "application/json");
				postRequest.setEntity(input);
				
				try {
			
					HttpResponse response = client.execute(postRequest);
					pageObj = IOUtils.toString(response.getEntity().getContent());
					System.out.println("INFO : Post Page Request Returned Status - " + response.getStatusLine().toString());
					JSONObject bodyPage = new JSONObject(pageObj);
					Long postid = bodyPage.getLong("id");
					
					System.out.println("INFO : Generated Page id - " + postid);
					listener.getLogger().println("INFO : Generated Page id - " + postid);
				
					try {
					
						// Get Page details with the found id 
						System.out.println("INFO : Fetching Details for the Generated Page");
						listener.getLogger().println("INFO : Fetching Details for the Generated Page");
					
						HttpGet get1PageRequest = new HttpGet(putContentRestUrl(postid,new String[] { "body.storage", "version", "ancestors" }));
						HttpResponse get1PageResponse = client.execute(get1PageRequest);
						get1pageEntity = get1PageResponse.getEntity();
						get1pageObj = IOUtils.toString(get1pageEntity.getContent());
						System.out.println("INFO : Fetching Details for the Generated Page Returned Status - " + get1PageResponse.getStatusLine().toString());
						listener.getLogger().println("INFO : Fetching Details for the Generated Page Returned Status - " + get1PageResponse.getStatusLine().toString());
					 
						JSONObject get1bodyPage = new JSONObject(get1pageObj);
					 
						String get1bodystorage = get1bodyPage.getJSONObject("body").getJSONObject("storage").getString("value");
						System.out.println("");
						System.out.println("INFO : Body Entries from Generated Page - " + get1bodystorage);
					 
						// Get the string between <table> and </table>
						Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
						Matcher m = p.matcher(get1bodystorage);
						while (m.find()) {
							tableoutput += m.group(1); 
						}
					 
						System.out.println("");
						System.out.println("INFO : Table Entries Caught Output - " + tableoutput);
					 
					}finally {
						if (get1pageEntity != null) {
							EntityUtils.consume(get1pageEntity);
						}
					}
				
					// Parse response into JSON
					JSONObject putpage = new JSONObject(get1pageObj);
					// List
					ArrayList<String[]> releaseinfodata = new ArrayList<String[]>();
					releaseinfodata.add(new String[] { CHNG, jobName, buildTimer, buildRevision, buildTag, buildURL, reportDate });
					String middleText = "";
					for (String[] data : releaseinfodata) {
						middleText = middleText + "<tr>" + "<td>" + data[0] + "</td>"
				             + "<td>" + data[1] + "</td>" + "<td>" + data[2]
				             + "</td>" + "<td>" + data[3] + "</td>" + "<td>" + data[4] + "</td>" + "<td>" + data[5] + "</td>" + "<td>" + data[6] + "</td>" +"</tr>";
					}
				 
					// Below data will be added to the above id found page
					String puttext = "<body>"
						 + "<h1>" + appname + " Release Info</h1>"
				         + "<table>"
						 + tableoutput + middleText + "</table>" + "</body>" + "<p> </p>"
						         + "<p> </p>" + "<p>"
						         + "<ac:structured-macro ac:name='gallery'>"
						         + "<ac:parameter ac:name='columns'>1</ac:parameter>"
						         + "</ac:structured-macro>" + "</p>";
				 
					putpage.getJSONObject("body").getJSONObject("storage").put("value", puttext);
					
					System.out.println("INFO : Incrementing Version for the Generated Page");
					listener.getLogger().println("INFO : Incrementing Version for the Generated Page");
					int currentVersion = putpage.getJSONObject("version").getInt("number");
					putpage.getJSONObject("version").put("number", currentVersion + 1);
				
					// Send update request
					HttpEntity put1PageEntity = null;
				 
					try {
						
						System.out.println("INFO : Adding Inputs from Jenkins Job to the Generated Page Table");
						listener.getLogger().println("INFO : Adding Inputs from Jenkins Job to the Generated Page Table");
				 
						HttpPut put1PageRequest = new HttpPut(putContentRestUrl(postid,new String[] {}));
						StringEntity entity = new StringEntity(putpage.toString(),ContentType.APPLICATION_JSON);
						put1PageRequest.setEntity(entity);
						HttpResponse putPageResponse = client.execute(put1PageRequest);
						
						System.out.println("INFO : Adding Inputs from Jenkins Job to the Generated Page Table Request Returned - " + putPageResponse.getStatusLine().toString());
						listener.getLogger().println("INFO : Adding Inputs from Jenkins Job to the Generated Page Table Request Returned - " + putPageResponse.getStatusLine().toString());
				 
						put1PageEntity = putPageResponse.getEntity();
						System.out.println("SUCCESS : Page Generated successfully with Jenkins job environment variables");
						listener.getLogger().println("SUCCESS : Page Generated successfully with Jenkins job environment variables");
						listener.getLogger().println("Release Info Page URL : " + BASE_URL + "/display/ds/" + testpagecoder);
						listener.getLogger().println("");
				 
					} finally {
						EntityUtils.consume(put1PageEntity);
					}
					//client.getConnectionManager().shutdown();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	else
	{
		System.out.println("INFO : Status Code 200");
		listener.getLogger().println("INFO : Status Code 200");
		System.out.println("INFO : Page Found");
		listener.getLogger().println("INFO : Page Found");
		
		JSONArray results = getbodyPage.getJSONArray("results");
		JSONObject first = results.getJSONObject(0);
		Long id = first.getLong("id"); // Get id of the found page
		System.out.println("INFO : Found ID - " + id);
		listener.getLogger().println("INFO : Found ID - " + id);
	
		try {
			// Get Page details with the found id 
			 System.out.println("INFO : Fetching Details for the Existing Page");
			 listener.getLogger().println("INFO : Fetching Details for the Existing Page");
			 
			 HttpGet get1PageRequest = new HttpGet(putContentRestUrl(id,new String[] { "body.storage", "version", "ancestors" }));
			 HttpResponse get1PageResponse = client.execute(get1PageRequest);
			 get1pageEntity = get1PageResponse.getEntity();
			 get1pageObj = IOUtils.toString(get1pageEntity.getContent());
			 
			 System.out.println("INFO : Fetching Details for the Existing Page Returned - " + get1PageResponse.getStatusLine().toString());
			 listener.getLogger().println("INFO : Fetching Details for the Existing Page Returned - " + get1PageResponse.getStatusLine().toString());
			 
			 JSONObject get1bodyPage = new JSONObject(get1pageObj);
			 String get1bodystorage = get1bodyPage.getJSONObject("body").getJSONObject("storage").getString("value");
			 System.out.println("INFO : Body Entries from Existing Page -" + get1bodystorage);
			 
			 // Get the string between <table> and </table>
			 Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			 Matcher m = p.matcher(get1bodystorage);
			 while (m.find()) {
			   tableoutput += m.group(1); 
			 }
			 
			 System.out.println("INFO : Table Entries from Existing Page Output : " + tableoutput);
			 
		}finally {
		if (get1pageEntity != null) {
		EntityUtils.consume(get1pageEntity);
		}
		}
		
		// Parse response into JSON
		 JSONObject putpage = new JSONObject(get1pageObj);
		// List
		 ArrayList<String[]> releaseinfodata = new ArrayList<String[]>();
		 releaseinfodata.add(new String[] { CHNG, jobName, buildTimer, buildRevision, buildTag, buildURL, reportDate });
		 String middleText = "";
		 for (String[] data : releaseinfodata) {
		     middleText = middleText + "<tr>" + "<td>" + data[0] + "</td>"
		             + "<td>" + data[1] + "</td>" + "<td>" + data[2]
		             + "</td>" + "<td>" + data[3] + "</td>" + "<td>" + data[4] + "</td>" + "<td>" + data[5] + "</td>" + "<td>" + data[6] + "</td>" + "</tr>";
		 }
		 
		 // Below data will be added to the above id found page
		 String puttext = "<body>"
				 + "<h1>" + appname + " Release Info</h1>"
		         + "<table>"
				 + tableoutput + middleText + "</table>" + "</body>" + "<p> </p>"
				         + "<p> </p>" + "<p>"
				         + "<ac:structured-macro ac:name='gallery'>"
				         + "<ac:parameter ac:name='columns'>1</ac:parameter>"
				         + "</ac:structured-macro>" + "</p>";
		 
		 putpage.getJSONObject("body").getJSONObject("storage").put("value", puttext);
		 
		 System.out.println("INFO : Incrementing Version for Existing Page");
		 listener.getLogger().println("INFO : Incrementing Version for Existing Page");
		 int currentVersion = putpage.getJSONObject("version").getInt("number");
		 putpage.getJSONObject("version").put("number", currentVersion + 1);
		
		 HttpEntity put1PageEntity = null;
		 
		 try {
			 
			 System.out.println("INFO : Adding Inputs from Jenkins Job to the Existing Page Table");
			 listener.getLogger().println("INFO : Adding Inputs from Jenkins Job to the Existing Page Table");
			 
			 HttpPut put1PageRequest = new HttpPut(putContentRestUrl(id,new String[] {}));
			 StringEntity entity = new StringEntity(putpage.toString(),ContentType.APPLICATION_JSON);
			 put1PageRequest.setEntity(entity);
		 
			 HttpResponse putPageResponse = client.execute(put1PageRequest);
		 
			 System.out.println("INFO : Adding Inputs from Jenkins Job to the Existing Page Table Request Returned " + putPageResponse.getStatusLine().toString());
			 listener.getLogger().println("INFO : Adding Inputs from Jenkins Job to the Existing Page Table Request Returned " + putPageResponse.getStatusLine().toString());
		 
		 put1PageEntity = putPageResponse.getEntity();
		 System.out.println("SUCCESS : Page Appended successfully with Jenkins job environment variables");
		 listener.getLogger().println("SUCCESS : Page Appended successfully with Jenkins job environment variables");
		 listener.getLogger().println("Release Info Page URL : " + BASE_URL + "/display/ds/" + testpagecoder);
		 listener.getLogger().println("");
		 
		 } finally {
		 EntityUtils.consume(put1PageEntity);
		 }		 
		
	}
		return result;
	}

}
