package com.example.quiltviewclient;

/**
* Quiltview - CMU 2013
* Author: Zhuo Chen <zhuoc@cs.cmu.edu>
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class StreamingThread extends Thread {
    private static final String LOG_TAG = "Streaming Thread";
    
    static final int BUFFER_SIZE = 102400;    // need to negotiate with server
    
    public static final int CODE_SEND_PACKET = 0;
    public static final int CODE_SEND_QUERY_ID = 1;
    public static final int CODE_SEND_CONTENT = 2;
    public static final int CODE_SEND_STOP = 3;
    public static final int CODE_SEND_USER_ID = 4;
    
    private Handler mHandler;
    
    private int protocolIndex;  // may use a protocol other than UDP
    private DatagramSocket udpSocket;
    private Socket socket;
    private PrintWriter TCPWriter;
    private BufferedReader TCPReader;
    private InetAddress remoteIP;
    private int remotePort;
    
    private FileInputStream inputStream;
    private FileOutputStream localFileStream;
    private File videoFile = new File(Environment.getExternalStorageDirectory() + File.separator + "streaming.mp4");
    
    public StreamingThread(){
    }
	
    public StreamingThread(int protocol, String IPString, int port) {
        setProtocolIndex(protocol);
        udpSocket = null;
        socket = null;
        try {
            remoteIP = InetAddress.getByName(IPString);
        } catch (UnknownHostException e) {
            Log.e(LOG_TAG, "unknown host: " + e.getMessage());
        }
        remotePort = port;        
        
        try {
            localFileStream = new FileOutputStream(videoFile);
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "Error in preparing local output file: " + e.getMessage());
        }
    }
    
    public Handler getHandler() {
        return mHandler;
    }
    
    public void setProtocolIndex(int protocolIndex) {
        this.protocolIndex = protocolIndex;
    }
    
    private OutputStream outStream;
	
    public void run() {
        Log.i(LOG_TAG, "Streaming thread running");
        	
        try {
            switch (protocolIndex) {
            case 0:
                udpSocket = new DatagramSocket();
                udpSocket.setReceiveBufferSize(BUFFER_SIZE);
                udpSocket.setSendBufferSize(BUFFER_SIZE);
                udpSocket.connect(remoteIP, remotePort);
                Log.i(LOG_TAG, "Streaming channel connected to: " + 
                        udpSocket.getInetAddress().toString() + ":" + udpSocket.getPort());      
                break;
            case 1:
            	if ((socket != null) && (!socket.isClosed()))
            		socket.close();
            	socket = new Socket(Const.proxy_addr, 7950);
        		if (socket.isConnected())
        			Log.i("sendVideo", "Socket Connected");
        		else
        			Log.e("sendVideo", "Socket not connected!");
        		TCPReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        		TCPWriter = new PrintWriter(socket.getOutputStream(), true);	// true means automatic flush
        		try {
        			TCPWriter.println("QUERY PORT");
        			Log.i("sendVideo", "Send query for port");
        			String newLine = TCPReader.readLine();
        			Log.i("sendVideo", "Got port info:" + newLine);
        			remotePort = Integer.parseInt(newLine);
        			Log.i("sendVideo", "Got port to stream: " + remotePort);
				} catch (IOException e) {
					Log.e("sendVideo", "Failed to get stream port: " + e.toString());
				}
        		socket.close();
        		socket = new Socket("typhoon.elijah.cs.cmu.edu", remotePort);
        		if (socket.isConnected())
        			Log.i("sendVideo", "Streaming socket Connected");
        		else
        			Log.e("sendVideo", "Streaming socket not connected!");
        		outStream = socket.getOutputStream();
            }
        } catch (SocketException e) {
            Log.e(LOG_TAG, "Error in initializing socket: " + e.getMessage());
        } catch (IOException ex) {
    		Log.e(LOG_TAG, "Socket Problem: " + ex.toString());
    		return;
        }
        Looper.prepare();

        // It's really complex, and not sure needed, to handle this warning...
        mHandler = new Handler() {
            int bytes_count=0;
//            int packet_count = 0;
            
            public void handleMessage(Message msg_in) {
            	if (msg_in.what == CODE_SEND_USER_ID) {
            		Log.d(LOG_TAG, "Received user ID");
            		Bundle data = msg_in.getData();
                    int user_ID = data.getInt("user_ID");
                    Log.v(LOG_TAG, "Got user ID:" + user_ID);
                    byte[] userID = pack(user_ID);
            		try {
						outStream.write(userID);
					} catch (IOException e) {
						Log.e("sendVideo", "Socket Problem: " + e.toString());
					}
            		Log.i(LOG_TAG, "User ID sent");
            	}
            	if (msg_in.what == CODE_SEND_QUERY_ID) {
            		Log.d(LOG_TAG, "Received query ID");
            		Bundle data = msg_in.getData();
                    int query_ID = data.getInt("query_ID");
                    Log.v(LOG_TAG, "Got query ID:" + query_ID);
                    byte[] queryID = pack(query_ID);
            		try {
						outStream.write(queryID);
					} catch (IOException e) {
						Log.e("sendVideo", "Socket Problem: " + e.toString());
					}
            		Log.i(LOG_TAG, "Query ID sent");
            	}
            	if (msg_in.what == CODE_SEND_CONTENT) {
            		Log.d(LOG_TAG, "Received query content");
            		Bundle data = msg_in.getData();
                    String queryContent = data.getString("content");
                    Log.v(LOG_TAG, "Got query content:" + queryContent);
            		try {
            			byte[] queryLen = pack(queryContent.length());
                		outStream.write(queryLen);
                		byte[] query = queryContent.getBytes();
                		outStream.write(query);
					} catch (IOException e) {
						Log.e("sendVideo", "Socket Problem: " + e.toString());
					}
            		Log.i(LOG_TAG, "Query content sent");
            	}
                if (msg_in.what == CODE_SEND_PACKET) {
//                    long time1 = System.currentTimeMillis();
                	Log.v(LOG_TAG, "Received a message");
                    
                    Bundle data = msg_in.getData();
                    byte[] buffer = data.getByteArray("data");
                    Log.v(LOG_TAG, "Got a frame of " + buffer.length + " bytes");
                    
                    switch (protocolIndex){
                    case 0:
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            
                        try {
//                            localFileStream.write(buffer, 0, buffer.length);
//                            localFileStream.flush();  //TODO: need this?
                            udpSocket.send(packet);
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Error in sending packet: " + e.getMessage());
                        }
                        break;
                    case 1:
                    	try {
                    		byte[] bufferLen = pack(buffer.length);
                    		outStream.write(bufferLen);
							outStream.write(buffer);
						} catch (IOException e) {
							Log.e("sendVideo", "Socket Problem: " + e.toString());
						}
                    }
//                    bytes_count += buffer.length;
//                    Log.v(LOG_TAG, "Bytes sent in total: " + bytes_count);
//                    packet_count++;
                    //Log.v(LOG_TAG, "Packets sent in total: " + packet_count);
//                    long time2 = System.currentTimeMillis();
//                    Log.d(LOG_TAG, "elapse time: " + (time1 - data.getLong("time")) + ", " + (time2 - time1));
                }
                if (msg_in.what == CODE_SEND_STOP) { 
                	try {
                	    outStream.write(pack(77));
                		//socket.close();
            	    	Log.i("sendVideo", "Exited the procedure normally.");
            		}
            		catch (Exception ex) {
            			Log.e("sendVideo", "Cannot close socket.");
            		}
                }
            }
        };
        Looper.loop();
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
		
    public boolean stopStreaming() throws IOException {
        if (udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
        }
        if (inputStream != null)
            inputStream.close();
        if (localFileStream != null)
            localFileStream.close();
        inputStream = null;
        localFileStream = null;
		
        socket.close();
        
        return true;
    }
}
