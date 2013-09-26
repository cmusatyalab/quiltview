package com.example.quiltviewclient;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
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

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;
import com.google.android.youtube.player.YouTubeIntents;

public class MainActivity extends Activity {

	/*
	 * Configuration
	 */
	private final boolean SAVE_VIDEO_TO_SDCARD = true; //Save to SD card or internal locations
	private final boolean UPLOAD_WITH_YOUTUBE_INTENT = true;
	private final boolean RECORD_VIDEO_AUTOMATICALLY = true; 

	
	/*
	 * Show the video on device
	 */
	Uri mVideoUri = null;
	String mVideoPath = null;
	String mVideoName = null;
	VideoView mVideoView = null; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Create an instance of Camera
        mCamera = Camera.open();

        // Create Camera Preview and relate it to the camera
        mPreview = new CameraPreview(this);
        mPreview.setCamera(mCamera);
        
        view_camera = (FrameLayout)findViewById(R.id.camera_preview);
        view_camera.addView(mPreview);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	
	//TODO Not working!
	//return Youtube url
	//Instructions: https://developers.google.com/youtube/2.0/developers_guide_protocol_resumable_uploads?csw=1#Resumable_uploads
	private String YOUTUBE_DEVELOPER_KEY = "AI39si7740iA4BBcqmSnvTXVe7IzZSJ3ujVsiwd6TnzVnxDOmP-etlNwegBUwPFj-0re5u3fotJx-kyAY1vz962IQAK8Q2NBBg";
	private String updateToYouTube() {
		Log.i("updateToYouTube", "Entering");
		
		//Local video at mVideoUri
		String YoutubeUri = "";
		 
		
		String sUploadURL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
		URL UploadURL;
		HttpURLConnection connection = null;  
		
		try {
			UploadURL = new URL (sUploadURL);
			connection = (HttpURLConnection) UploadURL.openConnection();
			connection.setRequestMethod("POST");
			
/*			String temp = connection.getResponseMessage();
			if (temp != null)
				Log.i("temp", temp);
	*/		
			Log.i("updateToYouTube", "Got token!" + mToken);
			
			connection.setRequestProperty("Authorization", "Bearer " + mToken);
		    connection.setRequestProperty("GData-Version", "2");  
		    connection.setRequestProperty("X-GData-Key", "key=" + YOUTUBE_DEVELOPER_KEY);  
		    connection.setRequestProperty("Slug", mVideoName);  
		    connection.setRequestProperty("Content-Type", "application/atom+xml; charset=UTF-8");  
			
			//Send empty metadata
			String urlParameters = ""; //
			connection.setRequestProperty("Content-Length", "" + 
		               Integer.toString(urlParameters.getBytes().length));
					
		    connection.setUseCaches (false);
		    connection.setDoInput(true);
		    connection.setDoOutput(true);

		    
		    //Send request: Metadata
		    DataOutputStream wr = new DataOutputStream (
		                connection.getOutputStream ());
		    wr.writeBytes (urlParameters);
		    wr.flush ();
		    wr.close ();
		    Log.i("updateToYouTube", "Metadata Sent.");
		    
		    connection.connect();
		    
		    //Get Response
		    String status = connection.getResponseMessage();
		    int code = connection.getResponseCode();
		    if (status != null)
				Log.i("status", code + ": " + status);
			
/*	        BufferedReader in = new BufferedReader(
	        		new InputStreamReader(
                    connection.getInputStream()));
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) 
	        {
	        	Log.i("Response From YouTube", inputLine);
	        }
*/	        
	        
		}
		catch (MalformedURLException ex) {
			Log.e("Upload To YouTube", ex.toString());
		} 
		catch (IOException ex) {
			Log.e("Upload To YouTube", ex.toString());
		}
		
        
		return "";// YoutubeUri;
	}
	
	private String mAuthCode = "4/OSgN0SsgE7SfF2mPweKC-iDuXiL6.Uvy1lZwufiYQshQV0ieZDAqWaUCOggl";
	private String CLIENT_ID = "12958789053.apps.googleusercontent.com"; 
	private String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	
	private String mEmail = "wenlu.c.hu@gmail.com";
	private String mToken = null;
	
	@SuppressWarnings(value = { "unused" })
	private void GetTokenWithGoogleAuthUtil()
	{
		final Context context = this;
    	new Thread()
    	{
    	    @Override
    	    public void run()
    	    {
    	    	String token = "";
    			try {
    			    token = GoogleAuthUtil.getToken(context, mEmail, "oauth2:" + Scopes.PLUS_LOGIN /**/);
    			    Log.i("GoogleAuthUtil", "Got Token: " + token);
    			} catch (UserRecoverableAuthException e) {
    				Log.i("getToken", "Handling UserRecoverableAuthException");
    				startActivity(e.getIntent());
    			} catch(Exception ex) {
    				Log.e("GoogleAuthUtil", "Problem Getting Token", ex);
    			}
    			
    			mToken = token;
    			updateToYouTube();

    	    }
    	}.start();
		
	}
	

	@SuppressWarnings(value = { "unused" })
	private String getOAuthToken() {
		String sAccessTokenURL = "https://accounts.google.com/o/oauth2/token";
		URL AccessTokenURL;
		HttpURLConnection connection = null;  
		
		try {
			AccessTokenURL = new URL (sAccessTokenURL);
			connection = (HttpURLConnection) AccessTokenURL.openConnection();
			connection.setRequestMethod("POST");
						
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		    connection.setRequestProperty("", "");  
			
			
			//Send empty metadata
			String urlParameters = "code=" + mAuthCode + "&"
					+ "client_id=" + CLIENT_ID + "&" 
					//+ "client_secret" +
					+ "redirect_uri=" + "" + "&"
					+ "";
			connection.setRequestProperty("Content-Length", "" + 
		               Integer.toString(urlParameters.getBytes().length));
					
		    connection.setUseCaches (false);
		    connection.setDoInput(true);
		    connection.setDoOutput(true);

		    //Send request
		    DataOutputStream wr = new DataOutputStream (
		                connection.getOutputStream ());
		    wr.writeBytes (urlParameters);
		    wr.flush ();
		    wr.close ();
		}
		catch (MalformedURLException ex) {
			Log.e("Upload To YouTube", ex.toString());
		} 
		catch (IOException ex) {
			Log.e("Upload To YouTube", ex.toString());
		}
		
		return "";
	}
	
	private CameraRecordingThread cameraRecorder = null;
	private Camera mCamera = null;
    private CameraPreview mPreview = null;
	private FrameLayout view_camera = null;
	private UploadingThread uploadingThread = null;
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
        	Thread.sleep(10*1000); //record for 10 seconds
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
	private int ACTION_AUTH_TOKEN=2;
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
	    	GetTokenWithGoogleAuthUtil();// Will upload upon receiving token;
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
