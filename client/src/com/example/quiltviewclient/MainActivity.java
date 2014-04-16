
package com.example.quiltviewclient;

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
*/ 

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        triggerPull();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void triggerPull() {
        // Launch the background pulling service
        Intent msgIntent = new Intent(this, RequestPullingService.class);
        startService(msgIntent);

    }

    
}