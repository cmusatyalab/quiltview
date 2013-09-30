package com.example.quiltviewclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UploadingThread extends Thread {
	private String mVideoPath = null;
	public void setVideoPath (String videoPath) {
		mVideoPath = videoPath;
	}
	
	private Handler mHandler = null;
	public void setHandler (Handler handler) {
		mHandler = handler;
	}
	
	private String mQuery = null;
	public void setQuery(String query) {
		mQuery = query;
	}
	
	private int mQueryID = -1;
	public void setQueryID(int queryID) {
		mQueryID = queryID;
	}
	
	
	public void run() {
		sendVideo();
	}
	
	/*
	 * Send to Server
	 */
    public void sendVideo() {
    	Log.i("sendVideo", "Starting to send video");
    	
    	Socket socket = null;
    	
    	try {
    		socket = new Socket("typhoon.elijah.cs.cmu.edu", 7950);
    		if (socket.isConnected())
    			Log.i("sendVideo", "Socket Connected");
    		else
    			Log.e("sendVideo", "Socket not connected!");
    		OutputStream outStream = socket.getOutputStream();
            //InputStream inStream = socket.getInputStream();

    		Log.i("sendVideo", "Answer to Query #" + mQueryID + ": " + mQuery);
    		byte[] queryID = pack(mQueryID);
    		outStream.write(queryID);
    		byte[] queryLen = pack(mQuery.length());
    		outStream.write(queryLen);
    		byte[] query = mQuery.getBytes();
    		outStream.write(query);
    	
            Log.i("sendVideo", "Video address: " + mVideoPath);
            File pic = new File(mVideoPath);
            Log.i("sendVideo", "Video File Size: " + pic.length());
            FileInputStream picIn = new FileInputStream(mVideoPath);
    	    byte[] data = new byte[(int) pic.length()];
            picIn.read(data);
            picIn.close();
    	
    	    Log.i("sendVideo", "Read-in Data Size: " + data.length);
            byte[] requestLen = pack(data.length);
    		outStream.write(requestLen);
    		Log.i("sendVideo", "requestLen sent.");
    		Log.i("sendVideo", 
    				"data start: " + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3]);
    		Log.i("sendVideo", 
    				"data end: " + data[data.length-4] + ", " + data[data.length-3] 
    						+ ", " + data[data.length-2] + ", " + data[data.length-1]);
    		
    		outStream.write(data);
    		Log.i("sendVideo", "data sent.");
    		//outStream.flush();
    		//outStream.close();
    		
    		Message msg = new Message();
    		mHandler.sendMessage(msg);

    	} catch (IOException ex) {
    		Log.e("sendVideo", "Socket Problem: " + ex.toString());
    		return;
    	} catch (Exception ex) {
    		Log.e("sendVideo", "Unknown Socket Problem: " + ex.toString());
    	} finally {
    		try {
    			socket.close();
    	    	Log.i("sendVideo", "Exited the procedure normally.");
    		}
    		catch (Exception ex) {
    			Log.e("sendVideo", "Cannot close socket.");
    		}
    	}
    	
    }    
    //Big-Endian, usigned int
    private byte[] pack(int n)  {
    	if (n < 0) 
    	{
    		Log.e("TCPpacket", "Pack number negative: " + n);
    		return null;
    	}
    	ByteBuffer pump = ByteBuffer.allocate(4);
    	pump.putInt(n);
    	byte[] bytes = pump.array();
    	Log.i("TCPpacket", "Pack " + n + " to " + bytes[0] + ", " + bytes[1] + ", " + bytes[2]+ ", " + bytes[3]);
    	return bytes;
    }
	
}