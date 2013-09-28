package com.example.quiltviewclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;

import com.example.quiltviewclient.MainActivity.ResponseReceiver;

public class RequestPullingService extends IntentService {

	private final String LOG_TAG = "Pulling Thread";
	
	  /** 
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	  public RequestPullingService() {
	      super("RequestPullingService");
	  }

	  /**
	   * The IntentService calls this method from the default worker thread with
	   * the intent that started the service. When this method returns, IntentService
	   * stops the service, as appropriate.
	   */
	    public static final String PARAM_IN_MSG = "imsg";
	    public static final String PARAM_OUT_MSG = "omsg";
	    private static final int PullRequestLimit = 10;
	    private static final boolean InfiniteLoop = true;
	    private static final int PretendReceivingRequest = 3;
	    @Override
	    protected void onHandleIntent(Intent intent) {
	    	Log.i(LOG_TAG, "Handling new intent.");
	        
	        int count = 0;
	        while (InfiniteLoop || (count < PullRequestLimit)) {
		        SystemClock.sleep(3000); // 3 seconds
		        pullRequest();
		        count ++;
		        
//		        if (count == PretendReceivingRequest) {
		        	//recordForQuery("");
//		        }
	        }
	        
	     // processing done here….
	        Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
	        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	        sendBroadcast(broadcastIntent);
	    }
	    
	    public static final String RESPOND_INTENT_QUERY = "com.example.quiltviewclient.respondquery";
	    private void recordForQuery(String query) {
        	Intent respondIntent = new Intent(this, RespondActivity.class);
        	respondIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	respondIntent.putExtra(RESPOND_INTENT_QUERY, query);
        	startActivity(respondIntent);
	    }
	    
	    private void pullRequest() {
	        String resultTxt = " "
		            + DateFormat.format("MM/dd/yy h:mmaa", System.currentTimeMillis());
		    Log.i(LOG_TAG, "Want to pull here." + resultTxt);
		    
		    HttpURLConnection urlConnection = null;
		    try {
			    URL url = new URL("http://typhoon.elijah.cs.cmu.edu:8000/quiltview/latest/?user_id=1");
			    urlConnection = (HttpURLConnection) url.openConnection();
		        urlConnection.setRequestMethod("GET");
		        urlConnection.setRequestProperty("Content-Type", "application/json");
		        
		        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
		        	InputStream is = urlConnection.getInputStream();
		        	int responseLen = urlConnection.getContentLength();
		        	Log.i(LOG_TAG, "Response Len = " + responseLen);
		        	
		        	//Read the json file 
		        	byte[] jsonBuffer = new byte[responseLen];
		        	Log.i(LOG_TAG, "Response Len = " + is.read(jsonBuffer));
		        	String jsonString = new String(jsonBuffer, "UTF-8");
		        	Log.i(LOG_TAG, "Got response: " + jsonString);
		        	
		        	//Decode json file
//		        	 String jsonString = "{\"content\": \"What is the weather at Pittsburgh?\"}";
		                
		        	try {
		        	  JSONObject obj= (JSONObject) JSONValue.parse(jsonString);
		        	  Log.i(LOG_TAG, obj.getClass().toString());
		        	  String Query = obj.get("content").toString();
		        	  Log.i(LOG_TAG, Query);    
		        	  recordForQuery(Query);
		        	} catch (NullPointerException ex) {
		        		Log.i(LOG_TAG, "No valid query");
		        	}
		        	
		        } else {
		        	Log.e(LOG_TAG, "Response " + urlConnection.getResponseCode() 
		        			+ ":" + urlConnection.getResponseMessage());
		        }
		        
		    } catch (MalformedURLException ex) {
		    	Log.e(LOG_TAG, "", ex);
		    } catch (IOException ex) {
		    	Log.e(LOG_TAG, "", ex);
		    }
		    finally {
		    	if (urlConnection != null)
		    		urlConnection.disconnect();
		    }
		    
		    
	    }
	}	
