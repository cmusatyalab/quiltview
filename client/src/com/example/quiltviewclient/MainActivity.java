package com.example.quiltviewclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    
    private ResponseReceiver receiver;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
        
        triggerPull();
    }
    
    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void triggerPull() {
        // Launch an intent service to do some async work

        Intent msgIntent = new Intent(this, RequestPullingService.class);
        startService(msgIntent);

    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP = "com.example.quiltviewclient.MESSAGE_PROCESSED";
        @Override
        public void onReceive(Context context, Intent intent) {
        	
        	//Process received result here
        	//
        	
        }
        
    }
    
}