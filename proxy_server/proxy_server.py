# TODO: description

import socket
import struct
import os
from optparse import OptionParser
import Image, cv, cv2
from cStringIO import StringIO

import upload_youtube 
import post_video

TMP_VIDEO_NAME = "uploaded_video.avi"

#QUILTVIEW_URL = "http://typhoon.elijah.cs.cmu.edu:8000"
QUILTVIEW_URL = "http://quiltview.opencloudlet.org:8000"
VIDEO_RESOURCE = "/api/dm/video/"

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

def startServer(host, port, buf, options):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind((HOST, PORT))
    s.listen(0)
    while True:
        # STEP 1: receive video from Glass
        print "waiting for connection"
        conn, addr = s.accept()
        print 'Connected by', addr

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

        #video_file = open(TMP_VIDEO_NAME, 'w')
        frames = []
        data = conn.recv(4)
        while data: 
            frame_len = struct.unpack("!I", data)[0]
            print "Frame length = %d" % frame_len
            data = ""
            received_len = 0
            while received_len < frame_len:
                data_tmp = conn.recv(frame_len - received_len)
                data += data_tmp
                received_len += len(data_tmp)
            print "received %d bytes" % len(data)
            frame = processFrame(data)
            frames.append(frame)
            data = conn.recv(4)
        print "Connection terminated by the other side"

        # write to file
        videoWriter = cv.CreateVideoWriter(TMP_VIDEO_NAME, cv.CV_FOURCC('X', 'V', 'I', 'D'), 30, (320, 240), True)
        if not videoWriter:
            print "Error in creating video writer"
            sys.exit(1)
        else:
            for frame in frames:
                cv.WriteFrame(videoWriter, frame)
               
        conn.close()
        #video_file.close()

        # STEP 2: upload to Youtube, get url back
        options.title = "QuiltView: %s" % query_content
        video_watch_id = upload_youtube.initialize_upload(options)   # this function handles all uploading...

        # STEP 3: register new video at QuiltView
        new_video_entry = {"url" : "http://www.youtube.com/watch?v=%s" % video_watch_id, 
                           "owner" : "/api/dm/user/%d/" % user_ID, 
                           "query" : "/api/dm/query/%d/" % query_ID, 
                           "upload_location_lat" : "11.111111", 
                           "upload_location_lng" : "22.2222"
                          }  # some fields are random for now
        post_video.post(QUILTVIEW_URL, VIDEO_RESOURCE, new_video_entry)

        # cleaning
        #os.remove(TMP_VIDEO_NAME)

if __name__ == "__main__":
    parser = OptionParser()
    parser.add_option("--file", dest="file", help="Video file to upload",
      default=TMP_VIDEO_NAME)
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
    BUF = 1024
    startServer(HOST, PORT, BUF, options)
