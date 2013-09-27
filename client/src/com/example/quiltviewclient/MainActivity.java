package com.example.quiltviewclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
    }
    
    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void triggerPull(View target) {
        // Launch an intent service to do some async work

        EditText input = (EditText) findViewById(R.id.txt_input);
        String strInputMsg = input.getText().toString();

        Intent msgIntent = new Intent(this, RequestPullingService.class);
        msgIntent.putExtra(RequestPullingService.PARAM_IN_MSG, strInputMsg);
        startService(msgIntent);

    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP = "com.example.quiltviewclient.MESSAGE_PROCESSED";
        @Override
        public void onReceive(Context context, Intent intent) {
           
            // Update UI, new "message" processed by SimpleIntentService
           TextView result = (TextView) findViewById(R.id.txt_result);
           String text = intent.getStringExtra(RequestPullingService.PARAM_OUT_MSG);
           result.setText(text);
        }
        
    }
    
}