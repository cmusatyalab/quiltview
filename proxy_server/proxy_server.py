# TODO: description

import socket
import struct

import upload_youtube 

TYPE_JSON = 0
TYPE_LENGTH = 1
TYPE_VIDEO = 2

TMP_VIDEO_NAME = "uploaded_video.mp4"

def startServer(host, port, buf):
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
        arguments = {"file" :TMP_VIDEO_NAME, 
                     "title":"QuiltView response",
                     "description":"served as a video response to QuiltView service",
                     "category":"22",
                     "keywords":"",
                     "privacyStatus":"unlisted",
                    }
        video_watch_id = upload_youtube.initialize_upload(arguments)   # this function handles all uploading...

        # TODO: register new video at QuiltView

if __name__ == "__main__":
    HOST = ""
    PORT = 7950
    BUF = 1024
    startServer(HOST, PORT, BUF)
