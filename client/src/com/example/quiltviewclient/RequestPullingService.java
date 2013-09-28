package com.example.quiltviewclient;

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
	    private static final boolean InfiniteLoop = false;
	    private static final int PretendReceivingRequest = 3;
	    @Override
	    protected void onHandleIntent(Intent intent) {
	    	Log.i(LOG_TAG, "Handling new intent.");
	        
	        int count = 0;
	        String resultTxt = "";
	        while (InfiniteLoop || (count < PullRequestLimit)) {
		        SystemClock.sleep(3000); // 3 seconds
		        resultTxt = " "
		            + DateFormat.format("MM/dd/yy h:mmaa", System.currentTimeMillis());
		        Log.i(LOG_TAG, "Want to pull here." + resultTxt);
		        count ++;
		        
		        if (count == PretendReceivingRequest) {
		        	Intent respondIntent = new Intent(this, RespondActivity.class);
		        	respondIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        	startActivity(respondIntent);
		        }
	        }
	        
	     // processing done here….
	        Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
	        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	        sendBroadcast(broadcastIntent);
	    }
	}	
