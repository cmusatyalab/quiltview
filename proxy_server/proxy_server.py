# TODO: description

import socket
import struct
import os
from optparse import OptionParser

import upload_youtube 
import post_video

TYPE_JSON = 0
TYPE_LENGTH = 1
TYPE_VIDEO = 2

TMP_VIDEO_NAME = "uploaded_video.mp4"

QUILTVIEW_URL = "http://typhoon.elijah.cs.cmu.edu:8000"
VIDEO_RESOURCE = "/api/dm/video/"


def startServer(host, port, buf, options):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind((HOST, PORT))
    s.listen(0)
    while True:
        # STEP 1: receive video from Glass
        print "waiting for connection"
        conn, addr = s.accept()
        print 'Connected by', addr
        current_type = TYPE_LENGTH
        while True:
            data = conn.recv(buf)
            if not data: 
                print "Connection terminated by the other side"
                break
            if current_type == TYPE_LENGTH:
                video_len = struct.unpack("!I", data)[0]  # Zhuo: why use this?
                print "Length = %d" % video_len
                video_file = open(TMP_VIDEO_NAME, 'w')
                current_type = TYPE_VIDEO
            elif current_type == TYPE_VIDEO:
                video_file.write(data)
                
        conn.close()
        video_file.close()

        # STEP 2: upload to Youtube, get url back
        video_watch_id = upload_youtube.initialize_upload(options)   # this function handles all uploading...

        # STEP 3: register new video at QuiltView
        new_video_entry = {"url" : "www.youtube.com/watch?v=%s" % video_watch_id, 
                           "owner" : "/api/dm/user/1/", 
                           "query_ID" : "/api/dm/query/1/", 
                           "upload_location_lat" : "11.111111", 
                           "upload_location_long" : "22.2222"
                          }  # some fields are random for now
        post_video.post(QUILTVIEW_URL, VIDEO_RESOURCE, new_video_entry)

        # cleaning
        os.remove(TMP_VIDEO_NAME)

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
