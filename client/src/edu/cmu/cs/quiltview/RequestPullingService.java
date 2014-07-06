package edu.cmu.cs.quiltview;

/**
* Quiltview - CMU 2013
* Author: Wenlu Hu <wenlu@cmu.edu>
* 
* Copyright (C) 2011-2013 Carnegie Mellon University
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
*
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*   
* The RequestPullingService class is built upon sample codes from "Training 
* for Android developers", which can be found at 
* https://developer.android.com/training/run-background-service/create-service.html
*/ 

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;

public class RequestPullingService extends IntentService {

	private final String LOG_TAG = "Pulling Thread";
	
	  public RequestPullingService() {
	      super("RequestPullingService");
	  }
	  
	  @Override
	  public void onCreate() {
		  super.onCreate();
	      locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	  }

	    public static final String PARAM_IN_MSG = "imsg";
	    public static final String PARAM_OUT_MSG = "omsg";
	    private static final int PullRequestLimit = 10;
	    private static final boolean InfiniteLoop = true;
	    
	    private String mSerialNumber = null;  
	    
	    @Override
	    protected void onHandleIntent(Intent intent) {
	    	//String macAddr = null;
	    	//Log.i(LOG_TAG, "Got mac address: " + macAddr);
	    	mSerialNumber = android.os.Build.SERIAL;
	    	Log.i(LOG_TAG, "Got serial number: " + mSerialNumber);
	    	
	    	Log.i(LOG_TAG, "Handling new intent.");
	        
	        int count = 0;
	        while (InfiniteLoop || (count < PullRequestLimit)) {
		        SystemClock.sleep(3000); // 3 seconds
		        pullRequest();
		        count ++;
	        }
	        
	    }
	    
	    public static final String RESPOND_INTENT_QUERY = "com.example.quiltviewclient.respondquery";
	    public static final String RESPOND_INTENT_QUERY_ID = "com.example.quiltviewclient.respondqueryID";
	    public static final String RESPOND_INTENT_USER_ID = "com.example.quiltviewclient.responduserID";
	    public static final String RESPOND_INTENT_QUERY_IMAGE = "com.example.quiltviewclient.queryImage";

	    private void recordForQuery(String query, int queryID, int userID, String imagePath) {
        	Intent respondIntent = new Intent(this, RespondActivity.class);
        	respondIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	respondIntent.putExtra(RESPOND_INTENT_QUERY, query);
        	respondIntent.putExtra(RESPOND_INTENT_QUERY_ID, queryID);
        	respondIntent.putExtra(RESPOND_INTENT_USER_ID, userID);
        	respondIntent.putExtra(RESPOND_INTENT_QUERY_IMAGE, imagePath);
        	startActivity(respondIntent);
	    }
	    
    	// Acquire a reference to the system Location Manager
    	LocationManager locationManager = null;

    	// Define a listener that responds to location updates
    	LocationListener locationListener = new LocationListener() {
    	    public void onLocationChanged(Location location) {
    	      // Called when a new location is found by the network location provider.
    	      mLocation = location; //Update with new Location
    	      mLocationUpdated = true;
    	      locationManager.removeUpdates(locationListener);
    	    }

    	    public void onStatusChanged(String provider, int status, Bundle extras) {}
    	    public void onProviderEnabled(String provider) {}
    	    public void onProviderDisabled(String provider) {}
    	  };
    	  
    	private Location mLocation = null;
    	private boolean mLocationUpdated = false;
    	
    	private void getLocation() {
    		mLocationUpdated = false;
    		
    		/* It is said that Google Glass has built-in GPS hardware. Unfortunately, 
    		 * it is not activated at this time. So GPS is not available when Glass
    		 * is not paired with a phone. During the evolution of Glass software, 
    		 * sometimes it just gives you a null result for GPS location, sometimes
    		 * it raises an exception. 
    		 * 
    		 * In this case, we get location from Network instead, which is not as 
    		 * precise, but always works.
    		 * Wenlu Hu, April 2014
    		 */
    		
		    //String locationProvider = LocationManager.GPS_PROVIDER;
    		String locationProvider = LocationManager.NETWORK_PROVIDER;

    		// Register the listener with the Location Manager to receive location updates
    		try {
    			locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
    		} catch (IllegalArgumentException ex) {
    			Log.i(LOG_TAG, "GPS provider not available");
    			mLocation = null;
    		}

		    if (mLocation == null)
		    	mLocation = locationManager.getLastKnownLocation(locationProvider);
	    }
	    
    	private String saveImageToLocal (String remotePath) {
    		if (remotePath.length() <= 0) return "";
    		
    		Log.i(LOG_TAG, "Entering saveImageToLocal");
    		String localPath = Environment.getExternalStorageDirectory()
    		        + "/QuiltView/" + "query_image.jpg";
	    	try {
				URL imageUrl = new URL(remotePath);
		      	InputStream in = new BufferedInputStream(imageUrl.openStream());
		      	ByteArrayOutputStream out = new ByteArrayOutputStream();
		      	byte[] buf = new byte[1024];
		      	int n = 0;
		      	while (-1!=(n=in.read(buf)))
		      	{
		      		out.write(buf, 0, n);
		      	}
		      	out.close();
		      	in.close();
		      	byte[] response = out.toByteArray();
		      	Log.i(LOG_TAG, "Image file size: " + response.length + "byte");
		      	FileOutputStream fos = new FileOutputStream(localPath);
		      	fos.write(response);
		      	fos.close();
		      	Log.i(LOG_TAG, "Image saved to local: " + localPath);
		  	} catch (IOException e) {
		  		Log.e(LOG_TAG, "Faile to save image to local", e);
		  	}
	    	return localPath;
    	}
    	
	    private void pullRequest() {
	        String resultTxt = " "
		            + DateFormat.format("MM/dd/yy h:mmaa", System.currentTimeMillis());
		    Log.i(LOG_TAG, "Begin pull." + resultTxt);
		    
		    getLocation();
		    double latitude, longitude;
		    if (mLocation != null)
		    {
		    	Log.i(LOG_TAG, "Real Location");
		    	latitude = mLocation.getLatitude();
		    	longitude = mLocation.getLongitude();
		    } else {
		    	//TODO test real location
		    	/*
		    	 * As we usually develop and demo indoor, the GPS location is not always 
		    	 * available. For the convenience of development, we use theses fixed fake 
		    	 * location. This is somewhere on Carnegie Mellon University campus
		    	 * Wenlu Hu, April 2014
		    	 */
		    	Log.i(LOG_TAG, "Fake Location");
		    	latitude = 40.443469; //40.44416720;
		    	longitude = -79.943862; //-79.94336060;
		    }
	    	Log.i(LOG_TAG, "Location: " + latitude 
	    			+ ", " + longitude);
		    
		    HttpURLConnection urlConnection = null;
		    try {
			    URL url = new URL(Const.quiltview_server_addr + "/latest/"
			    		+ "?user_id=" + mSerialNumber 
			    		+ "&lat=" + latitude + "&lng=" + longitude);
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
		        	
		        	try {
                        JSONObject obj= (JSONObject) JSONValue.parse(jsonString);
                        String query = obj.get("content").toString();
                        int queryID = Integer.parseInt(obj.get("query_id").toString());
                        int userID = Integer.parseInt(obj.get("user_id").toString());
                        String imagePath = obj.get("image").toString();
                        Log.i(LOG_TAG, userID + ", " + queryID + ": " + query + "&" + imagePath);    
                        imagePath = saveImageToLocal(imagePath);

                        recordForQuery(query, queryID, userID, imagePath);
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
