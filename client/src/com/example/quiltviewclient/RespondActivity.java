package com.example.quiltviewclient;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.youtube.player.YouTubeIntents;

public class RespondActivity extends Activity {

	/*
	 * Configuration
	 */
	private final boolean SAVE_VIDEO_TO_SDCARD = true; //Save to SD card or internal locations
	private final boolean UPLOAD_WITH_YOUTUBE_INTENT = true;
	private final boolean RECORD_VIDEO_AUTOMATICALLY = true; 

	
	private UploadingThread uploadingThread = null;

	Uri mVideoUri = null;
	String mVideoPath = null;
	String mVideoName = null;
	VideoView mVideoView = null; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_respond);
		
		// Create an instance of Camera
        mCamera = Camera.open();

        // Create Camera Preview and relate it to the camera
        mPreview = new CameraPreview(this);
        mPreview.setCamera(mCamera);
        
        view_camera = (FrameLayout)findViewById(R.id.camera_preview);
        view_camera.addView(mPreview);

		displayQuery();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCamera.release();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void displayQuery() {
		Intent queryIntent = getIntent();
		String query = queryIntent.getStringExtra(RequestPullingService.RESPOND_INTENT_QUERY);
		TextView textView = (TextView) findViewById(R.id.status_update);
		textView.setText(query);
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
			sendVideoToServer();
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
        uploadingThread.start();
	}
	
	private CameraRecordingThread cameraRecorder = null;
	private Camera mCamera = null;
    private CameraPreview mPreview = null;
	private FrameLayout view_camera = null;
	
	private void takeVideoWithCameraAPI() throws IOException {
		
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
        } catch (IOException e) {
            Log.e("takeVideoWithCameraAPI", "Error in setting preview display: " + e.getMessage());
        }
        
        mCamera.startPreview();
        
        cameraRecorder = null;
		if (cameraRecorder == null){
            cameraRecorder = new CameraRecordingThread();
            Log.i("takeVideoWithCameraAPI", "Created a new camera recording thread");
        }
        
        cameraRecorder.setCamera(mCamera);
        cameraRecorder.setPreviewSurface(mPreview.getHolder().getSurface());
        
        cameraRecorder.start();//run() in a new thread
        
        try
        {
        	Thread.sleep(3*1000); //record for 10 seconds
        } catch (InterruptedException ex) {
        	Log.e("takeVideoWithCameraAPI", "Sleep Interrupted" );
            try
            {
            	Thread.sleep(10*1000); //record for 10 seconds
            } catch (InterruptedException exception) {
            	//Failed again
            	//Let it be.
            }        	
        }
        
        //Finish recording
        cameraRecorder.stopCapturing();
        try {
        	Thread.sleep(1000); //record for 10 seconds
        } catch (InterruptedException ex) {
        	
        }
        mVideoPath = cameraRecorder.getVideoPath();
        
        
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
	
}
