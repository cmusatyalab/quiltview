package com.example.quiltviewclient;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.VideoView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
	
	//Todo
	//return Youtube url
	private String updateToYouTube() {
		//Local video at mVideoUri
		
		String YoutubeUri = "";
		return YoutubeUri;
	}
	
	private int ACTION_TAKE_VIDEO=1; //??? todo
	private void dispatchTakeVideoIntent() {
	    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
	    startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		handleCameraVideo(data);
	}
	
	/*
	 * Show the video on device
	 */
	Uri mVideoUri;
	VideoView mVideoView; 
	
	private void handleCameraVideo(Intent intent) {
	    mVideoUri = intent.getData();
	    mVideoView = (VideoView) findViewById(R.id.replay_videoview);
	    mVideoView.setVideoURI(mVideoUri);
	    mVideoView.start();
	}
}
