package com.example.quiltviewclient;


import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.VideoView;

import com.google.android.youtube.player.YouTubeIntents;



public class MainActivity extends Activity {

	private boolean SAVE_VIDEO_TO_SDCARD = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
/*	    service = com.google.api.services.tasks.Tasks.builder(transport, jsonFactory)
		        .setApplicationName("Google-TasksAndroidSample/1.0")
		        .setHttpRequestInitializer(credential)
		        .setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY))
		        .build();
*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/* Take a 10-sec video 
	 * Update to YouTube
	 */
	public void recordVideo(View view) {
		//recordVideo
		dispatchTakeVideoIntent();
		
		//Uploade Video to YouTube
		String YoutubeUri = updateToYouTube();
		
		//Send YoutubeUri to server
	}
	
	//TODO
	//return Youtube url
	//Instructions: https://developers.google.com/youtube/2.0/developers_guide_protocol_resumable_uploads?csw=1#Resumable_uploads
	private String YOUTUBE_DEVELOPER_KEY = "AI39si7iKjn3EmnLceHn0N8keveSb1JC0gTC2XL665evKHap2nTmULv8Z-2jeVMljiRxcqw6FKMaoY1I7drU4zfPp4bdGLid6w";
	
	private String updateToYouTube() {
		//Local video at mVideoUri


		/*
		 String YoutubeUri = "";
		 
		
		String sUploadURL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
		URL UploadURL;
		HttpURLConnection connection = null;  
		
		try {
			UploadURL = new URL (sUploadURL);
			connection = (HttpURLConnection) UploadURL.openConnection();
			connection.setRequestMethod("POST");
			
			//TODO place it here or once per app lifetime?
			getOAuthToken();
			
			connection.setRequestProperty("Authorization", "Bearer ???");//TODO
		    connection.setRequestProperty("GData-Version", "2");  
		    connection.setRequestProperty("X-GData-Key", "key=" + YOUTUBE_DEVELOPER_KEY);  
		    connection.setRequestProperty("Slug", mVideoName);  
			
			
			//Send empty metadata
			String urlParameters = ""; //
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
		*/
        
		return "";// YoutubeUri;
	}
	
	private String mAuthCode = "4/OSgN0SsgE7SfF2mPweKC-iDuXiL6.Uvy1lZwufiYQshQV0ieZDAqWaUCOggl";
	private String CLIENT_ID = "12958789053.apps.googleusercontent.com"; 
	private String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	
	//private HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public void getAuthInfo(View view) {
		
/*		GoogleAuthorizationCodeGrant authRequest = new GoogleAuthorizationCodeGrant(transport,
				jsonFactory, oauthConfig.getOauthClientId(), oauthConfig.getOauthClientSecret(),
				oauthConfig.getOauthAuthorizationCode(), oauthConfig.getOauthRedirectUri());
		authRequest.useBasicAuthorization = false;
		AccessTokenResponse authResponse  = authRequest.execute();
		oauthConfig.setOauthAccessToken(authResponse.accessToken);
		oauthConfig.setOauthRefreshToken(authResponse.refreshToken);

		String UserID = "wenlu.c.hu@gmail.com";
		AccessMethod method; //?
		
		AuthorizationCodeFlow codeFlow = new AuthorizationCodeFlow(method , transport, null, null, null, UserID, UserID);
		.loadCredential(UserID);
		 
		String url = "https://accounts.google.com/o/oauth2/auth?"
				+ "client_id=" + CLIENT_ID + "&"
				+ "redirect_uri=" + REDIRECT_URI + "&" //localhost?
				+ "scope=https://gdata.youtube.com&"
				+ "response_type=code&"
				+ "access_type=offline";
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
*/
		//TODO
		//Retrieve Auth Code from Google's response
		//Now cheating
		mAuthCode = "4/OSgN0SsgE7SfF2mPweKC-iDuXiL6.Uvy1lZwufiYQshQV0ieZDAqWaUCOggl";
		
//		getOAuthToken();
		
		
	}
/*	
	GoogleCredential credential = new GoogleCredential();

	  com.google.api.services.tasks.Tasks service;

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	  }

	  private void chooseAccount() {
	    accountManager.manager.getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE,
	        AUTH_TOKEN_TYPE,
	        null,
	        TasksSample.this,
	        null,
	        null,
	        new AccountManagerCallback<Bundle>() {

	          public void run(AccountManagerFuture<Bundle> future) {
	            Bundle bundle;
	            try {
	              bundle = future.getResult();
	              setAccountName(bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
	              setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
	              onAuthToken();
	            } catch (OperationCanceledException e) {
	              // user canceled
	            } catch (AuthenticatorException e) {
	              Log.e(TAG, e.getMessage(), e);
	            } catch (IOException e) {
	              Log.e(TAG, e.getMessage(), e);
	            }
	          }
	        },
	        null);
	  }
*/	
	
/*	private void TestGoogleAuthUtil(String mEmail, String mScope, String token)
	{

		try {
		    token = GoogleAuthUtil.getToken(this, mEmail, mScope);
		} catch {
		}
	}*/
	
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
    private String VIDEO_FILE_SUFFIX=".3gp";
    
    //mVideoUri
    //private String mCurrentPhotoPath;
    //default: 3gp?
    //Create an empty 3gp file for the camera to store captured video
    private File createVideoFile() throws IOException {
        // Create an image file name
        //String timeStamp = 
        //    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = VIDEO_FILE_PREFIX /* + timeStamp + "_"*/;
    	Log.i("AlbumDir", "Image File Name: " + imageFileName);
    	
    	File storageDir = 
    		getAlbumDir(); 
    		//this.getFilesDir();
    	
    	Log.i("AlbumDir", storageDir.toString());
    			
    	if (!storageDir.exists())
    	{
    		Log.e("AlbumDir", "ERROR: Does not exist!!!");
    	}
        File image = File.createTempFile(
            imageFileName,
            VIDEO_FILE_SUFFIX,
            storageDir
        );
        if (image.exists())
        {
        	Log.d("AlbumDir", "image created");
        }
        mVideoPath = image.getAbsolutePath(); 
    	Log.i("AlbumDir", "mVideoPath set: " + mVideoPath);
        return image;
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
			handleCameraVideo(data);
		else
		if (requestCode == ACTION_UPLOAD_VIDEO)
		{
			//Log.i("UploadVideo", data.getData().toString());
		}
	}
	
	/*
	 * Show the video on device
	 */
	Uri mVideoUri;
	String mVideoPath;
	String mVideoName;
	VideoView mVideoView; 
	
	private void handleCameraVideo(Intent intent) {
		mVideoView = (VideoView) findViewById(R.id.replay_videoview);
	    
		if (SAVE_VIDEO_TO_SDCARD) {
			String[] parseVideoPath = mVideoPath.split("/");
		    mVideoName = parseVideoPath[parseVideoPath.length-1];	
		    Log.i("Saving Video Locally", "Name: " + mVideoName);
		    mVideoView.setVideoPath(mVideoPath);
		} else{
			mVideoUri = intent.getData();
			Log.i("Saving Video Locally", "URI: " + mVideoUri);
			mVideoView.setVideoURI(mVideoUri);
		}
	    
	    mVideoView.start();
	    
		//Uri localVideoUri = Uri.fromFile( new File (mVideoPath));
        //Intent intent = YouTubeIntents.createUploadIntent(this, localVideoUri);
		Intent youtubeIntent = YouTubeIntents.createUploadIntent(this, mVideoUri);
        startActivityForResult(youtubeIntent, ACTION_UPLOAD_VIDEO);
        Log.i("Upload2Youtube", "Uploading");
        
        
	}
}
