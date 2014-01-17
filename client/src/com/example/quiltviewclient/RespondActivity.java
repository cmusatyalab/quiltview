package com.example.quiltviewclient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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

import com.google.android.gms.plus.model.people.Person.Image;
import com.google.android.youtube.player.YouTubeIntents;


public class RespondActivity extends Activity {

	/*
	 * Configuration
	 */
	private final boolean SAVE_VIDEO_TO_SDCARD = true; //Save to SD card or internal locations
	private final boolean UPLOAD_WITH_YOUTUBE_INTENT = true;
	private final boolean RECORD_VIDEO_AUTOMATICALLY = true; 
	
	private static final int LOCAL_OUTPUT_BUFF_SIZE = 1024 * 100;
	
	private boolean hasStarted = false;
	
	private UploadingThread uploadingThread = null;

	Uri mVideoUri = null;
	String mVideoPath = null;
	String mVideoName = null;
	VideoView mVideoView = null; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED+
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		setContentView(R.layout.activity_respond);

        extractAndDisplayQuery();


	      
        // Launching Camera App using voice command need to wait.  
        // See more at https://code.google.com/p/google-glass-api/issues/list
        try {
        	Thread.sleep(1000);
        } catch (InterruptedException e) {}
        
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


	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("OnDestroy", "prepare to exit activity");
		mCamera.release();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	String mQuery = null;
	int mQueryID = -1;
	int mUserID = -1;
	String mQueryImagePath = null;
	private void extractAndDisplayQuery() {
		Intent queryIntent = getIntent();
		mQuery = queryIntent.getStringExtra(RequestPullingService.RESPOND_INTENT_QUERY);
		mQueryID = queryIntent.getIntExtra(RequestPullingService.RESPOND_INTENT_QUERY_ID, -1);
		mUserID = queryIntent.getIntExtra(RequestPullingService.RESPOND_INTENT_USER_ID, -1);
		mQueryImagePath = queryIntent.getStringExtra(RequestPullingService.RESPOND_INTENT_QUERY_IMAGE);
		
		/*
		 * Show Query
		 */
		TextView textView = (TextView) findViewById(R.id.status_update);
		//textView.setText(mQueryID + ": " + mQuery);
		textView.setText("QuiltView:\n" + mQuery + "?");
		
		/*
		 * Show Query Image
		 */
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
  	  		//img.setImageResource(R.drawable.ic_launcher);
  	  	}
		
	}
	
	/* Take a 10-sec video 
	 * Send it to the server
	 */
	public void recordVideo(View view) throws IOException {
		
		//recordVideo
		if (RECORD_VIDEO_AUTOMATICALLY)
		{
			takeVideoWithCameraAPI();
			//playVideo();
			//uploadVideo();
			//sendVideoToServer();
		}
		else
		{
			dispatchTakeVideoIntent();
		}
		
	}
	
	private void sendVideoToServer() {
        uploadingThread = new UploadingThread();
        uploadingThread.setVideoPath(mVideoPath);
        uploadingThread.setHandler(mHandler);
        uploadingThread.setQuery(mQuery);
        uploadingThread.setQueryID(mQueryID);
        
        uploadingThread.start();
	}
	
	private CameraRecordingThread cameraRecorder = null;
	private StreamingThread streamingThread = null;
	private Camera mCamera = null;
    private CameraPreview mPreview = null;
	private FrameLayout view_camera = null;
	private ByteArrayOutputStream bufferedOutput;
	private Handler streamingHandler;
	private long startedTime;
	
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
                if (System.currentTimeMillis() - startedTime > 5 * 1000) {
                	hasStarted = false;
                	msg_out = Message.obtain();
                	msg_out.what = StreamingThread.CODE_SEND_STOP;
                	streamingHandler.sendMessage(msg_out);
                	streamingThread = null;
                	finish();
                }
            }
        }
    };
	
	private void takeVideoWithCameraAPI() throws IOException {
		Button record_button = (Button) findViewById(R.id.record_button);
		record_button.setEnabled(false);
		view_camera.setVisibility(View.VISIBLE);
		TextView textView = (TextView) findViewById(R.id.status_update);
		textView.setText("Recording...");
		
		Log.i("takeVideoWithCameraAPI", "Starting to record");
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
            Log.i("takeVideoWithCameraAPI", "Setted preview display");
        } catch (IOException e) {
            Log.e("takeVideoWithCameraAPI", "Error in setting preview display: " + e.getMessage());
        }
        
        mCamera.startPreview();
        Log.i("takeVideoWithCameraAPI", "Started preview");
        
//		if (cameraRecorder == null){
//            cameraRecorder = new CameraRecordingThread();
//            Log.i("takeVideoWithCameraAPI", "Created a new camera recording thread");
//        }
//        
//        cameraRecorder.setCamera(mCamera);
//        cameraRecorder.setPreviewSurface(mPreview.getHolder().getSurface());
        
        mPreview.setPreviewCallback(previewCallback);
        Log.i("takeVideoWithCameraAPI", "Added preview callback");
        
        //cameraRecorder.start();//run() in a new thread
        
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
        
        startedTime = System.currentTimeMillis();
        hasStarted = true;
//        record_button.setVisibility(View.GONE);
        
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
        
        //Finish recording
//        cameraRecorder.stopCapturing();
//        try {
//        	Thread.sleep(1000); //record for 10 seconds
//        } catch (InterruptedException ex) {
//        	
//        }
//        mVideoPath = cameraRecorder.getVideoPath();
        
        
	}

	
	private int ACTION_TAKE_VIDEO=1; 
	private int ACTION_UPLOAD_VIDEO=3;
	private void dispatchTakeVideoIntent() {
	    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
	    
	    try {
		    //Save it to SD card.
	    	if (SAVE_VIDEO_TO_SDCARD)
	    	{
			    File f = createVideoFile();
		    	takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
	    	}
	    	Log.i("dispatchTakeVideoIntent", "Launching camera");
	    	startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
	    } catch (Exception ex) {
	    	Log.e("dispatchTakeVideoIntent", ex.toString(), ex);
	    	//Log.e("dispatchTakeVideoIntent", ex.getStackTrace());
	    }
	}
	
    private String VIDEO_FILE_PREFIX="Quiltview";
    private String VIDEO_FILE_SUFFIX=".mp4";
    
    //mVideoUri
    //default: mp4
    //Create an empty file for the camera to store captured video
    private File createVideoFile() throws IOException {
        // Create an video file name
        //String timeStamp = 
        //    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = VIDEO_FILE_PREFIX /* + timeStamp + "_"*/;
    	Log.i("AlbumDir", "Video File Name: " + videoFileName);
    	
    	File storageDir = 
    		getAlbumDir(); 
    		//this.getFilesDir();
    	
    	Log.i("AlbumDir", storageDir.toString());
    			
    	if (!storageDir.exists())
    	{
    		Log.e("AlbumDir", "ERROR: Does not exist!!!");
    	}
        File video = File.createTempFile(
            videoFileName,
            VIDEO_FILE_SUFFIX,
            storageDir
        );
        if (video.exists())
        {
        	Log.d("AlbumDir", "video created");
        }
        mVideoPath = video.getAbsolutePath(); 
    	Log.i("AlbumDir", "mVideoPath set: " + mVideoPath);
        return video;
    }

    private File getAlbumDir() {
    	File storageDir = new File (
    		    Environment.getExternalStorageDirectory()
    		        + "/QuiltView/"
    		       // + getAlbumName()
		);
 
 		if (!storageDir.exists()) {
 			storageDir.mkdirs();
 			Log.w("AlbumDir", "*******Photo Directory Created*********");
 		} else { // do nothing
 		}
 		
    	if (storageDir.exists())  	{
    		Log.i("AlbumDir", "Exist");
    	}
    	else	{
    		Log.e("AlbumDir", "ERROR: Does not exist!!!");
    	}
    	return storageDir;
    }
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTION_TAKE_VIDEO)
		{
			handleCameraVideo(data);
		}
		else
		if (requestCode == ACTION_UPLOAD_VIDEO)
		{
			//Log.i("UploadVideo", data.getData().toString());
		}
	}
	
	/*
	 * Show the video on device
	 */

	/*
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

	/*
	 * Uploade Video to YouTube
	 */
	private void uploadVideo() {
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
	    	/*
	    	 * Abandoned feature
	    	 * Found no way to upload to youtube without starting youtube activity
	    	 * with intent.
	    	 */
	    	
	    	//GetTokenWithGoogleAuthUtil();// Will upload upon receiving token;
	    }
	
	}
	
	private void handleCameraVideo(Intent intent) {
	    
		if (SAVE_VIDEO_TO_SDCARD) {
			String[] parseVideoPath = mVideoPath.split("/");
		    mVideoName = parseVideoPath[parseVideoPath.length-1];	
		    Log.i("Saving Video Locally", "Name: " + mVideoName);
    		mVideoUri = Uri.fromFile( new File (mVideoPath));
	    } else{
			mVideoUri = intent.getData();
			mVideoPath = mVideoUri.getPath();
			Log.i("Saving Video Locally", "URI: " + mVideoUri);
		}
	    
		//playVideo();
		//uploadVideo();
		sendVideoToServer();
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (controlThread != null) {
//                Message msg_out = Message.obtain();
//                msg_out.what = ControlThread.CODE_CLOSE_CONNECTION;
//                controlHandler.sendMessage(msg_out);
//            }                
		    	
            finish();
            return true;
        }
		    
        return super.onKeyDown(keyCode, event);
    }
}
