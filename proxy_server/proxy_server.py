#!/usr/bin/env python 
#
# QuiltView: a Crowd-Sourced Video Response System
#
#   Author: Zhuo Chen <zhuoc@cs.cmu.edu>
#
#   Copyright (C) 2011-2013 Carnegie Mellon University
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

from multiprocessing import Process, Manager
import socket
import struct
import os
import time
from optparse import OptionParser
import Image, cv, cv2
from cStringIO import StringIO

import upload_youtube 
import post_video
import Const

def processFrame(data):

    #with open('ttt.jpg', 'w') as f:
    #    f.write(data)
    file_jpgdata = StringIO(data)
    image = Image.open(file_jpgdata)
    frame_bgr = cv.CreateImageHeader(image.size, cv.IPL_DEPTH_8U, 3)
    cv.SetData(frame_bgr, image.tostring())
    # convert frame from BGR to RGB
    # in Image, it's RGB; in cv, it's BGR
    frame = cv.CreateImage(cv.GetSize(frame_bgr),cv.IPL_DEPTH_8U, 3)
    cv.CvtColor(frame_bgr, frame, cv.CV_BGR2RGB)
    #cv.SaveImage('ttt.jpg', frame)

    # Show real-time captured image
    #cv.NamedWindow('captured video', cv.CV_WINDOW_AUTOSIZE)
    #cv.ShowImage('captured video', frame) 
    #cv.WaitKey(1)

    return frame

def _receive_all(conn, target_len):
    data = ""
    received_len = 0
    while received_len < target_len:
        data_tmp = conn.recv(target_len - received_len)
        data += data_tmp
        received_len += len(data_tmp)
    return data

def serverNewClient(queue, options):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind(("", 0))
    port = s.getsockname()[1]
    queue.put(port)
    s.listen(0)
    while True:
        # STEP 1: receive video from Glass
        print "proxy: waiting for connection"
        conn, addr = s.accept()
        print 'proxy: Connected by', addr

        data = conn.recv(4)
        user_ID = struct.unpack("!I", data)[0]
        print "User ID = %d" % user_ID
        data = conn.recv(4)
        query_ID = struct.unpack("!I", data)[0]
        print "Query ID = %d" % query_ID
        data = conn.recv(4)
        content_len = struct.unpack("!I", data)[0]
        print "Content length = %d" % content_len
        query_content = ''
        if not content_len == 0:
            query_content = conn.recv(content_len)
        print "Query content = %s" % query_content

        #video_file = open(Const.TMP_VIDEO_NAME, 'w')
        frames = []
        #conn.settimeout(3)
        data = _receive_all(conn, 4)
        while data: 
            try:
                frame_len = struct.unpack("!I", data)[0]
                if frame_len == 77: # magic number for now
                    break
                #print "Frame length = %d" % frame_len
                data = _receive_all(conn, frame_len)
                print "received %d bytes at port %d" % (len(data), port)
                frame = processFrame(data)
                frames.append(frame)
                data = conn.recv(4)
            except:
                print "timeout"
                break
        print "Connection terminated by the other side"

        # write to file
        video_file_name = Const.TMP_VIDEO_NAME + "%d_%d.avi" % (query_ID, port)
        videoWriter = cv.CreateVideoWriter(video_file_name, cv.CV_FOURCC('X', 'V', 'I', 'D'), 15, (320, 240), True)
        if not videoWriter:
            print "Error in creating video writer"
            sys.exit(1)
        else:
            for frame in frames:
                cv.WriteFrame(videoWriter, frame)
               
        conn.close()
        #video_file.close()

        # STEP 2: upload to Youtube, get url back
        if Const.ID_UPLOAD_YOUTUBE:
            options.title = "QuiltView: %s" % query_content
            options.file = video_file_name 
            video_watch_id = upload_youtube.initialize_upload(options)   # this function handles all uploading...

        # STEP 3: register new video at QuiltView
        if Const.ID_UPLOAD_YOUTUBE:
            video_url = "http://www.youtube.com/watch?v=%s" % video_watch_id
        else:
            video_url = Const.QUILTVIEW_URL + "/media/" + video_file_name

        new_video_entry = {"url" : video_url, 
                           "owner" : "/api/dm/user/%d/" % user_ID, 
                           "query" : "/api/dm/query/%d/" % query_ID, 
                           "upload_location_lat" : "11.111111", 
                           "upload_location_lng" : "22.2222"
                          }  # some fields are random for now
        post_video.post(Const.QUILTVIEW_URL, Const.VIDEO_RESOURCE, new_video_entry)

        # cleaning
        #os.remove(video_file_name)

        break

def startServer(host, port, options):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind((host, port))
    s.listen(0)

    queue = Manager().Queue()
    while True:
        print "main: waiting for connection"
        conn, addr = s.accept()
        print 'main: Connected by', addr

        data = conn.recv(1024)
        print 'received port request'
        p = Process(target = serverNewClient, args = (queue, options, ))
        p.start()
        while queue.empty():
            time.sleep(0.05)
            print "queue is still empty"
        port = queue.get()
        conn.sendall(str(port) + '\r\n')
        print "assigned port %d to new client" % port


if __name__ == "__main__":
    parser = OptionParser()
    parser.add_option("--file", dest="file", help="Video file to upload",
      default=Const.TMP_VIDEO_NAME)
    parser.add_option("--title", dest="title", help="Video title",
      default="QuiltView response")
    parser.add_option("--description", dest="description", help="Video description",
      default="served as a video response to QuiltView service")
    parser.add_option("--category", dest="category", help="Video category",
      default="22")  # seems to be "people and blogs"
    parser.add_option("--keywords", dest="keywords",
      help="Video keywords, comma separated", default="")
    parser.add_option("--privacyStatus", dest="privacyStatus", help="Video privacy status",
      default="unlisted")
    (options, args) = parser.parse_args()

    HOST = ""
    PORT = 7950
    startServer(HOST, PORT, options)
