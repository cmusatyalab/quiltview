from urlparse import urlparse
import httplib
import json
from pprint import pprint

def post(global_url, resource_url, json_string):
    print("Posting to %s%s" % (global_url, resource_url))
    end_point = urlparse("%s%s" % (global_url, resource_url))
    #print end_point[1]

    params = json.dumps(json_string)
    headers = {"Content-type":"application/json" }
    print params

    # this is only for https connection
    conn = httplib.HTTPConnection(end_point[1])
    conn.request("POST", "%s" % end_point[2], params, headers)
    response = conn.getresponse()
    data = response.read()
    json_response = json.loads(data)
    conn.close()

    return json_response

if __name__ == "__main__":
    # just for test
    pprint(post("http://128.2.213.116", "/api/dm/video/", {"url":"http://www.youtube.com/watch?v=G5SKlOkHDgM", "owner":"/api/dm/user/6/", "query":"/api/dm/query/65/", "upload_location_lat":"11.111111", "upload_location_lng":"22.2222"}))
