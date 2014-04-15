import sys
import random
import thread
import time
import multiprocessing
import json
import requests
from pprint import pprint
import csv

sys.path.insert(0, '../proxy_server/')
import post_video
import upload_youtube

QUILTVIEW_URL = "http://quiltview.opencloudlet.org"
VIDEO_RESOURCE = "/api/dm/video/"

class VirtualUser(multiprocessing.Process):
    
    def __init__(self, uid, lat, lng, dailyLimit, respondProb):
        multiprocessing.Process.__init__(self)
        self.uid = "VIRTUAL" + uid
        self.lat = lat
        self.lng = lng
        self.respondProb = respondProb

        fakeemail = self.uid + "@example.com"
        self.register(self.uid, fakeemail, dailyLimit)

    def run(self):
        proc_name = self.name
        print "Starting Virtual User #%s \tlat=%f \tlng=%f" % (self.uid, self.lat, self.lng)
        pullcount = 0;
        while (pullcount < 10) :
            time.sleep(2) 
            self.pullAndRespond()
            #pullcount += 1
        return

    def register(self, uid, uemail, dailyLimit) :
        print "Register #%s %s" % (uid, uemail)
        prefDic = {'min_reward': 5, 
                   'active_hours' : '9am-5pm' }
        preference = json.dumps(prefDic)
        pprint(post_video.post(QUILTVIEW_URL, "/api/dm/user/", {"location_lat":"0", "location_lng":"0", "google_account":uemail, "uuid":uid, "max_upload_time": dailyLimit, "other_preferences":preference}))


    def updateVideo(self, query_content, query_id, user_id) :
        videoID = random.randint(0,4)
        FAKE_VIDEO_NAME = "fakevideo" + str(videoID) + ".mp4"
        print "Uploading " + FAKE_VIDEO_NAME + " to YouTube..."
        from optparse import OptionParser
        parser = OptionParser()
        parser.add_option("--file", dest="file", help="Video file to upload",
          default=FAKE_VIDEO_NAME)
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
        options.title = "QuiltView: %s" % query_content
        video_watch_id = upload_youtube.initialize_upload(options)   # this function handles all uploading...

        # register new video at QuiltView
        new_video_entry = {"url" : "http://www.youtube.com/watch?v=%s" % video_watch_id,
                           "owner" : "/api/dm/user/%d/" % user_id,
                           "query" : "/api/dm/query/%d/" % query_id, 
                           "upload_location_lat" : "11.111111",
                           "upload_location_lng" : "22.2222"
                          }  # some fields are random for now
        post_video.post(QUILTVIEW_URL, VIDEO_RESOURCE, new_video_entry)

    def pullAndRespond(self):
        print "Pull #%s @(%f,%f)" % (self.uid, self.lat, self.lng)
        '''
            https://quiltview.opencloudlet.org/latest/
            ?user_id=<mSerialNumber>
            &lat=<latitude>
            &lng=<longitude>
        '''
        url = QUILTVIEW_URL + "/latest/" + \
                   "?user_id=%s&lat=%f&lng=%f" % (self.uid, self.lat, self.lng)
        r = requests.get(url)
        json_result = r.json()
            
        print json_result
        #if json_result != "{}" :
        if json_result.__len__() != 0 :
            query_content = json_result['content'] 
            query_id = json_result['query_id']
            user_id = json_result['user_id']
            print "Got a response. Upload to Youtube"
            #Upload video to Youtube
            #respondProb = 0.5
            rolldice = random.random() #[0.0, 1.0)
            if (rolldice < self.respondProb) :
                print "Decided to respond"
                self.updateVideo(query_content, query_id, user_id)
            else:
                print "Decided NOT to respond"


def create():
    usage = "python virtual_glass.py create <number of users> <query limit/day> <google map link> <respond probability>"  
    '''
    <google map link> example
    https://maps.google.com/?ll=40.442758,-79.942338&spn=0.00743,0.015814&t=m&z=17
    '''
    if len(sys.argv) != 6 :
        print usage
        exit(0)
    else :
        nUser = int (sys.argv[2])
        dailyLimit = int (sys.argv[3])
        mapUrl = sys.argv[4]
        respondProb = float (sys.argv[5])

    from urlparse import urlparse
    from urlparse import parse_qs
    httpQuery = urlparse(mapUrl).query
    #print httpQuery
    httpQueryDic = parse_qs(httpQuery)
    ll = httpQueryDic["ll"][0].split(",")
    spn = httpQueryDic["spn"][0].split(",")
    #print "ll=" + str(ll)
    #print "spn=" + str(spn)
    lat_center = float(ll[0])
    lng_center = float(ll[1])
    #print "lat=%f \t lng=%f" % (lat_center, lng_center)

    lat_spn = float(spn[0])
    lng_spn = float(spn[1])

    print "Creating %d virtual glass users,\n with daily limit of %d queries,\n around (%f, %f) \n with responding probability %f " \
        % (nUser, dailyLimit, lat_center, lng_center, respondProb)
    #patchID
    #A time stamp for this patch of virtual glasses
    #To add to userID
    import time
    ts = time.time()
    import datetime
    st = datetime.datetime.fromtimestamp(ts).strftime('%Y%m%d-%H:%M:%S')
    patchID = st

    mapFileName = "VirtualUsers.csv"
    f = open(mapFileName, 'a')
    #f.write('Latitude,Longitude,Description,Icon,PID\n')
    import os
    pid = os.getpid()
    f.write('Latitude,Longitude,Description,Icon,' + str(pid) + '\n')
    for i in range(0, nUser) :
        rand = random.random() - 0.5; # -0.5 ~ 0.5
        lat_i = lat_center + rand * lat_spn;
        rand = random.random() - 0.5; # -0.5 ~ 0.5
        lng_i = lng_center + rand * lng_spn;
        uid = patchID + "~" + str(i)
        print "Starting Virtual User #%s lat=%f \tlng=%f" % (uid, lat_i, lng_i)
        
        #new thread
        newUser = VirtualUser(uid, lat_i, lng_i, dailyLimit, respondProb)
        newUser.start()
        f.write( str(lat_i) + "," + str(lng_i) + "," + "VIRTUAL" + uid  + ',,' + str(newUser.pid) + '\n') 

    f.close()

def stopall() :
    fileName = "VirtualUsers.csv"
    with open(fileName, 'rb') as csvfile:
        reader = csv.reader(csvfile, delimiter=',')
        for row in reader:
            pid = row[4]
            userID = row[2]
            if pid != "PID" :
                print "stopping user %s @PID%s" % (userID, pid)

                #kill all relevant processes
                import os
                import signal
                os.kill(int(pid), signal.SIGKILL)
    
    return

def resumeall() :
    #TODO
    return

def killall() :
    fileName = "VirtualUsers.csv"
    with open(fileName, 'rb') as csvfile:
        reader = csv.reader(csvfile, delimiter=',')
        for row in reader:
            userID = row[2]
            #TODO send DELETE userID requests to QuiltView Service
            
    #TODO delete file
    return

if __name__ == "__main__":
    usage = "python virtual_glass.py <command create/stopall/resumeall/killall> <number of users> <google map link>"
    '''
    <google map link>
    for example:
    "https://maps.google.com/?ll=40.442758,-79.942338&spn=0.00743,0.015814&t=m&z=17"
    '''
    if len(sys.argv) < 2 :
        print usage
        exit(0)
    else :
        command = sys.argv[1]

    options = { "create" : create,
                "stopall" : stopall,
                "resumeall" : resumeall,
                "killall" : killall}
    options[command]()
    
