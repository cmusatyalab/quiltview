package com.example.quiltviewclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.youtube.player.YouTubeIntents;


public class RespondActivity extends Activity {
    private static final String LOG_TAG = "RespondActivity";

	/*
	 * Configuration
	 */
	private final boolean SAVE_VIDEO_TO_SDCARD = true; //Save to SD card or internal locations
	private final boolean UPLOAD_WITH_YOUTUBE_INTENT = true;
	private final boolean RECORD_VIDEO_AUTOMATICALLY = true; 
	
	private static final int LOCAL_OUTPUT_BUFF_SIZE = 1024 * 100;
	
	private volatile boolean hasStarted = false;
	
	private UploadingThread uploadingThread = null;
	
	private Timer auto_destroy_timer = null;
	private Timer record_timer = null;

	// Query info
	private String mQuery = null;
    private int mQueryID = -1;
    private int mUserID = -1;
    private String mQueryImagePath = null;

    // Local video info
    private Uri mVideoUri = null;
    private String mVideoPath = null;
    private String mVideoName = null;
    private VideoView mVideoView = null; 
    
    // Action code for intents
    private int ACTION_TAKE_VIDEO=1; 
    private int ACTION_UPLOAD_VIDEO=3;
    
    private CameraRecordingThread cameraRecorder = null;
    private StreamingThread streamingThread = null;
    private Camera mCamera = null;
    private CameraPreview mPreview = null;
    private FrameLayout view_camera = null;
    private ByteArrayOutputStream bufferedOutput;
    private Handler streamingHandler;
    private long startedTime;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// This is make the query automatically pop up even when Glass is in sleep
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED+
		        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	
		setContentView(R.layout.activity_respond);

        extractAndDisplayQuery();

        // Create an instance of Camera
        mCamera = Camera.open();
        Log.i("OnCreate", "camera opened");

        // Create Camera Preview and relate it to the camera
        mPreview = new CameraPreview(this);
        mPreview.setCamera(mCamera);
        Log.i("OnCreate", "camera set preview");
        
        view_camera = (FrameLayout)findViewById(R.id.camera_preview);
        view_camera.setVisibility(View.INVISIBLE);
        view_camera.addView(mPreview);
        
        auto_destroy_timer = new Timer();
        auto_destroy_timer.schedule(new TimerTask() {          
            @Override
            public void run() {
                if (!hasStarted) {
                    finish();
                }
            }
        }, 10000);  // vanish after several seconds the user not responding
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("OnDestroy", "prepare to exit activity");
		if (mCamera != null) {
		    mCamera.stopPreview();
		    mCamera.release();
		    mCamera = null;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	private void extractAndDisplayQuery() {
		Intent queryIntent = getIntent();
		mQuery = queryIntent.getStringExtra(RequestPullingService.RESPOND_INTENT_QUERY);
		mQueryID = queryIntent.getIntExtra(RequestPullingService.RESPOND_INTENT_QUERY_ID, -1);
		mUserID = queryIntent.getIntExtra(RequestPullingService.RESPOND_INTENT_USER_ID, -1);
		mQueryImagePath = queryIntent.getStringExtra(RequestPullingService.RESPOND_INTENT_QUERY_IMAGE);
		
		//Show Query
		TextView textView = (TextView) findViewById(R.id.status_update);
		textView.setText("QuiltView:\n" + mQuery + "?");
		
		//Show Query Image
		Log.i("RespondActivity", "mQueryImagePath length: "+ mQueryImagePath.length());
		ImageView img = (ImageView) findViewById(R.id.query_image);
		if (mQueryImagePath.length() > 0)
		{
			Log.i("RespondActivity", "Show query image @" + mQueryImagePath);
			File imgFile = new  File(mQueryImagePath);
			Log.i("RespondActivity", "Show query image @" + imgFile.getAbsolutePath());
			if(imgFile.exists()){
				Log.i("RespondActivity", "Query image exists!");
			    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				img.setImageBitmap(myBitmap);
			}
  	  	} else {
  	  		img.setImageResource(R.drawable.ic_launcher); //zhuoc: what image is this?
  	  	}
		
	}
	
	/*  
	 * Take a several (e.g. 5) second video, send it to the server (proxy)
	 * This function is called through the "Record" button
	 */
	public void recordVideo(View view) throws IOException {
		streamVideo();
		//playVideo();
		//uploadVideo();
		//sendVideoToServer();		
	}
	
	private PreviewCallback previewCallback = new PreviewCallback() {
        public void onPreviewFrame(byte[] frame, Camera mCamera) {
//            Log.v("OnPreviewCallBack", "got one frame");
            if ( hasStarted ) {
            	Log.v("OnPreviewCallBack", "got one frame to transmit");
                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size size = parameters.getPreviewSize();
                YuvImage image = new YuvImage(frame, parameters.getPreviewFormat(), size.width, size.height, null);
                image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 60, bufferedOutput);

                // now ask streaming thread to send packet
                // streamingHandler = streamingThread.getHandler();
                Message msg_out = Message.obtain();
                Bundle data = new Bundle();
                data.putByteArray("data", bufferedOutput.toByteArray());
                bufferedOutput.reset();
                msg_out.what = StreamingThread.CODE_SEND_PACKET;
                msg_out.setData(data);
                
                Log.v("OnPreviewCallBack", "sent a message");
                streamingHandler.sendMessage(msg_out);
//                if (System.currentTimeMillis() - startedTime > 5 * 1000) {
//                	
//                }
            }
        }
    };
	
	private void streamVideo() throws IOException {
		Button record_button = (Button) findViewById(R.id.record_button);
		record_button.setEnabled(false);
		view_camera.setVisibility(View.VISIBLE);
		TextView textView = (TextView) findViewById(R.id.status_update);
		textView.setText("Recording...");
		
		Log.i(LOG_TAG, "Starting to stream");
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
            Log.i("takeVideoWithCameraAPI", "Setted preview display");
        } catch (IOException e) {
            Log.e("takeVideoWithCameraAPI", "Error in setting preview display: " + e.getMessage());
        }
        
        mCamera.startPreview();
        Log.i("takeVideoWithCameraAPI", "Started preview");
        
        mPreview.setPreviewCallback(previewCallback);
        Log.i("takeVideoWithCameraAPI", "Added preview callback");
                
        if (streamingThread == null)
            streamingThread = new StreamingThread(1, "128.2.213.25", 7950);
        
        streamingThread.start();
        Log.i("takeVideoWithCameraAPI", "StreamingThread starts");
        
        bufferedOutput = new ByteArrayOutputStream(LOCAL_OUTPUT_BUFF_SIZE);
        streamingHandler = streamingThread.getHandler();
        while (streamingHandler == null) {
            try {
                Log.d("takeVideoWithCameraAPI", "Sleep a little bit to wait for the streaming Handler to get prepared");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e("takeVideoWithCameraAPI", "Error in sleeping...: " + e.getMessage());
            }
            streamingHandler = streamingThread.getHandler();
        }
        
        Log.d("takeVideoWithCameraAPI", "Now starting to stream");
        // send user ID
        Message msg_out = Message.obtain();
        Bundle data = new Bundle();
        data.putInt("user_ID", mUserID);
        msg_out.what = StreamingThread.CODE_SEND_USER_ID;
        msg_out.setData(data);
        streamingHandler.sendMessage(msg_out);
        // send query ID
        msg_out = Message.obtain();
        data = new Bundle();
        data.putInt("query_ID", mQueryID);
        msg_out.what = StreamingThread.CODE_SEND_QUERY_ID;
        msg_out.setData(data);
        streamingHandler.sendMessage(msg_out);
        
        msg_out = Message.obtain();
        data = new Bundle();
        data.putString("content", mQuery);
        msg_out.what = StreamingThread.CODE_SEND_CONTENT;
        msg_out.setData(data);
        streamingHandler.sendMessage(msg_out);
        
        hasStarted = true;
        auto_destroy_timer = new Timer();
        auto_destroy_timer.schedule(new TimerTask() {          
            @Override
            public void run() {
                hasStarted = false;
                Message msg_out = Message.obtain();
                msg_out.what = StreamingThread.CODE_SEND_STOP;
                streamingHandler.sendMessage(msg_out);
                streamingThread = null;
                finish();
            }
        }, 5000);  // vanish after several seconds sec
        record_button.setVisibility(View.GONE);
        
		/* 
		 * The part below tries to record a video locally and upload to Youtube from Glass.
		 * This is the ideal approach. But we currently can't find a way to upload to Youtube with Android.
		 * This part will be needed when upload_to_Youtube can work.
		 */
//		if (cameraRecorder == null){
//            cameraRecorder = new CameraRecordingThread();
//            Log.i("takeVideoWithCameraAPI", "Created a new camera recording thread");
//        }
//        
//        cameraRecorder.setCamera(mCamera);
//        cameraRecorder.setPreviewSurface(mPreview.getHolder().getSurface());
//        
//        cameraRecorder.start();//run() in a new thread
//		
//        try
//        {
//        	Log.d("takeVideoWithCameraAPI", "Sleep for 10 secs" );
//        	Thread.sleep(5*1000); //record for 10 seconds
//        } catch (InterruptedException ex) {
//        	Log.e("takeVideoWithCameraAPI", "Sleep Interrupted" );
//            try
//            {
//            	Thread.sleep(10*1000); //record for 10 seconds
//            } catch (InterruptedException exception) {
//            	//Failed again
//            	//Let it be.
//            }        	
//        }
//        
//        //Finish recording
//        cameraRecorder.stopCapturing();
//        try {
//        	Thread.sleep(1000); //record for 10 seconds
//        } catch (InterruptedException ex) {
//        	
//        }
//        mVideoPath = cameraRecorder.getVideoPath();
        
	}
		
	/*
	// Show the video on device
	private void playVideo() {
		mVideoView = (VideoView) findViewById(R.id.replay_videoview);
		if (SAVE_VIDEO_TO_SDCARD) {
			mVideoView.setVideoPath(mVideoPath);
		} else {
			mVideoView.setVideoURI(mVideoUri);
		}
	    mVideoView.start();
	}
	*/

    private Handler mHandler = new Handler () {
    	@Override
    	public void handleMessage(Message msg) {
    		TextView textView = (TextView) findViewById(R.id.status_update);
    		textView.setText("Video successfully sent.");
    	}
    };
    
    private void sendVideoToProxy() {
        uploadingThread = new UploadingThread();
        uploadingThread.setVideoPath(mVideoPath);
        uploadingThread.setHandler(mHandler);
        uploadingThread.setQuery(mQuery);
        uploadingThread.setQueryID(mQueryID);
        
        uploadingThread.start();
    }

    /*
	 * Uploade Video to YouTube
	 * This function is not called now because we are streaming video directly to the proxy server
	 */
    private void uploadToYoutube() {
        if (UPLOAD_WITH_YOUTUBE_INTENT)
        {
            /* TODO
             * Currently YouTubeIntents.createUploadIntent() does not work with a 
             * URI created from external file. It only works with content:// when 
             * Camera activity stores the video file in internal storage. That is, 
             * SAVE_VIDEO_TO_SDCARD = false at the beginning of the source code. 
             */
   	
            Intent youtubeIntent = YouTubeIntents.createUploadIntent(this, mVideoUri);
            startActivityForResult(youtubeIntent, ACTION_UPLOAD_VIDEO);
	        Log.i("Upload2Youtube", "Uploading");
	    }
        else
        {
            //Abandoned feature: Found no way to upload to youtube without starting youtube activity with intent.
            //GetTokenWithGoogleAuthUtil();// Will upload upon receiving token;
        }
    }
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {		    	
            finish();
            return true;
        }
		    
        return super.onKeyDown(keyCode, event);
    }
}
