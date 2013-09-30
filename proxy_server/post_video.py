from urlparse import urlparse
import httplib
import json
from pprint import pprint

def post(global_url, resource_url, json_string):
    print("Posting to %s%s" % (global_url, resource_url))
    end_point = urlparse("%s%s" % (global_url, resource_url))

    params = json.dumps(json_string)
    headers = {"Content-type":"application/json" }
    print params

    conn = httplib.HTTPConnection(end_point[1])
    conn.request("POST", "%s" % end_point[2], params, headers)
    response = conn.getresponse()
    data = response.read()
    json_response = json.loads(data)
    conn.close()

    return json_response

if __name__ == "__main__":
    # just for test
    pprint(post("http://typhoon.elijah.cs.cmu.edu:8000", "/api/dm/user/", {"location_lat":"11.111111", "location_long":"22.2222", "google_account":"wenlu.c.hu",}))
    #pprint(post("http://typhoon.elijah.cs.cmu.edu:8000", "/api/dm/query/", {"requester":"/api/dm/user/1/", "latest_upload_time":"2013-09-26", "interest_location_lat":"11.111111", "interest_location_long":"22.2222", "time_out":5, "accepted_staleness":5, "reward":1}))
    #pprint(post("http://typhoon.elijah.cs.cmu.edu:8000", "/api/dm/video/", {"url":"http://www.youtuble.com", "owner":"/api/dm/user/1/", "query":"/api/dm/query/1/", "upload_location_lat":"11.111111", "upload_location_long":"22.2222"}))
