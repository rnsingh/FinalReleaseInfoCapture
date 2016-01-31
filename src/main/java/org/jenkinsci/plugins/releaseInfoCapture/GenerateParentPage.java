package org.jenkinsci.plugins.releaseInfoCapture;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import hudson.model.BuildListener;

public class GenerateParentPage {

	private static final String BASE_URL = "https://alim4azad.atlassian.net/wiki";
	private static final String USERNAME = "azadal";
	private static final String PASSWORD = "azadal@2012";
	private static final String ENCODING = "utf-8";

	private static String postContentRestUrl() throws UnsupportedEncodingException {
		// RestApi call for authorization and posting a new page within the required space
		return String.format("%s/rest/api/content/?os_authType=basic&os_username=%s&os_password=%s",BASE_URL,URLEncoder.encode(USERNAME, ENCODING),URLEncoder.encode(PASSWORD, ENCODING));
 }
	
	private static String getContentRestUrl(final String title,final String spaceKey) throws UnsupportedEncodingException {
		// RestApi call for authorization and search page by title and spaceKey
		return String.format("%s/rest/api/content?title=%s&spaceKey=%s&os_authType=basic&os_username=%s&os_password=%s",BASE_URL,title,spaceKey,URLEncoder.encode(USERNAME, ENCODING),URLEncoder.encode(PASSWORD, ENCODING));
 }

	public static Long GetParentPage(BuildListener listener) throws ClientProtocolException, IOException
	{
		DefaultHttpClient client = new DefaultHttpClient();
		String pageObj = null;
		HttpEntity getPageEntity = null;
		String parentpage = "MT Release Info Notes"; //Parent Page
		Integer size;
		Long postid = null;
		
		String text = "<body>"
				 + "<h1>" + parentpage + "</h1>"
				 + "</body>" + "<p> </p>"
		         + "<p> </p>" + "<p>"
		         + "<ac:structured-macro ac:name='gallery'>"
		         + "<ac:parameter ac:name='columns'>1</ac:parameter>"
		         + "</ac:structured-macro>" + "</p>";
		String json = "{\"type\":\"page\",\"title\":\"" + parentpage + "\",\"space\":{\"key\":\"ds\"},\"body\":{\"storage\":{\"value\":\"" + text + "\",\"representation\":\"storage\"}}}";
	
			System.out.println("INFO : Checking Parent Page Existence");
			listener.getLogger().println("");
			listener.getLogger().println("INFO : Checking Parent Page Existence");
			String testpagecoder = URLEncoder.encode(parentpage, ENCODING);
			HttpGet getPageRequest = new HttpGet(getContentRestUrl(testpagecoder,"ds")); // Search in space "ds"
			HttpResponse getPageResponse = client.execute(getPageRequest);
			getPageEntity = getPageResponse.getEntity();
			String getPageObj = IOUtils.toString(getPageEntity.getContent());
			
			System.out.println("INFO : Get Parent Page Details Request Returned Status - " + getPageResponse.getStatusLine().toString());
			listener.getLogger().println("INFO : Get Parent Page Details Request Returned Status - " + getPageResponse.getStatusLine().toString());
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
					System.out.println("INFO : Generating Parent Page " + parentpage);
					listener.getLogger().println("INFO : Generating Parent Page " + parentpage);
					HttpPost postRequest = new HttpPost(postContentRestUrl());
					String url = URLEncoder.encode(parentpage, ENCODING);
					StringEntity input = new StringEntity(json);
					postRequest.setHeader("Accept", "application/json");
					postRequest.setHeader("Content-type", "application/json");
					postRequest.setEntity(input);
					
					try {
					
						HttpResponse response = client.execute(postRequest);
						pageObj = IOUtils.toString(response.getEntity().getContent());
						//System.out.println("" + pageObj);
						System.out.println("INFO : Post Parent Page Request Returned Status - " + response.getStatusLine().toString());
						listener.getLogger().println("INFO : Post Parent Page Request Returned Status - " + response.getStatusLine().toString());
						JSONObject bodyPage = new JSONObject(pageObj);
						postid = bodyPage.getLong("id");
						System.out.println("INFO : Generated Parent Page id - " + postid);
						listener.getLogger().println("INFO : Generated Parent Page id - " + postid);
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
				postid = first.getLong("id"); // Get id of the found page
				System.out.println("INFO : Parent Page Found ID - " + postid);
				listener.getLogger().println("INFO : Parent Page Found ID - " + postid);
				
				
			}
			return postid;
			} 
}
