/*
 * Original code from Zhuo Chen: package edu.cmu.cs.elijah.catchattention;
 * Modified for QuiltView byt Wenlu Hu
 */

package com.example.quiltviewclient;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

public class CameraRecordingThread extends Thread {
    private static final String LOG_TAG = "Camera Recording Thread";
    
    private static final int LOCAL_BUFF_SIZE = 102400;
    
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    
    private MediaRecorder videoRecorder;    
    private Camera camera;
    private Surface previewSurface;
    private boolean isRecording;
    
    public CameraRecordingThread() {
        videoRecorder = null;
        camera = null;
        isRecording = false;
        previewSurface = null;
    }
	
    public void setPreviewSurface(Surface surface) {
        previewSurface = surface;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }
    
    public void run() {
        //FileDescriptor fd = localSender.getFileDescriptor();    // this is where the video data writes to
        
        try {
        	Log.i(LOG_TAG, "Start Video Captureing");
            startVideoCapturing();
            Thread.sleep(30);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error starting video capture: " + e.getMessage());
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Error in sleeping: " + e.getMessage());
        }
    }

    private String mVideoPath;
    public String getVideoPath() {
    	if (mVideoPath != null)
    		return mVideoPath;
    	else
    		return "";
    }
    
    //open camera, configure video recorder, start recording and write the data to fd
    private boolean startVideoCapturing() throws IOException {  	
    	Log.i("VideoCapture", "Starting Video Capturing");
        videoRecorder = new MediaRecorder();
    	
        // TODO: understand this...
        camera.unlock();
    	
        videoRecorder.setCamera(camera);
        
        videoRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        videoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    	
        // a profile contains a group of pre-configured settings, use a profile or configure all settings
        CamcorderProfile cameraProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        videoRecorder.setProfile(cameraProfile);
    	
        File video = createVideoFile();
        mVideoPath = video.getAbsolutePath();
        Log.i(LOG_TAG, "Video Path: " + mVideoPath);
        Log.i(LOG_TAG, "Video Path: " + video.toString());
        videoRecorder.setOutputFile(video.toString());
        videoRecorder.setVideoFrameRate(30); //TODO Root for StartFail on Glass
        
        videoRecorder.setPreviewDisplay(previewSurface);
        // if any of the above configuration is missing, prepare() will fail
    	
        try {
            videoRecorder.prepare();
            Log.v("camera","prepared");
			
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed preparing video recorder: " + e.getMessage());
            stopCapturing();
            return false;
        }
    	
        Log.i(LOG_TAG, "Started Video Captureing");
        videoRecorder.start();
        isRecording = true;

        return true;
    }
	
    public void stopCapturing() throws IOException {
    	Log.i(LOG_TAG, "Stop Video Captureing");
        if (videoRecorder != null) {
            if (isRecording)
                videoRecorder.stop();
			
            videoRecorder.reset();
            videoRecorder.release();
        }
		
        if (camera != null){
            camera.lock();
            //Wenlu: Release it in MainActivity.OnDestroy();
            //camera.release();
            camera = null;
        }
		
    	Log.i(LOG_TAG, "Stopped Video Captureing");
    }
    
    private String VIDEO_FILE_PREFIX="Quiltview";
    private String VIDEO_FILE_SUFFIX=".mp4";

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

}
