import requests
from urlparse import urlparse
from urlparse import parse_qs

GOOGLE_LOCATION_API = "http://maps.googleapis.com/maps/api/geocode/json"

def getLocationFromAddress(address):
    url = GOOGLE_LOCATION_API + "?address=%s&sensor=false" % address
    print("Connecting to %s" % (url))

    r = requests.get(url)
    json_result = r.json()
    location = json_result['results'][0]['geometry']['location']

    return (location['lat'], location['lng'])

def getLocationFromLink(link):
    httpQuery = urlparse(link).query
    print httpQuery
    httpQueryDic = parse_qs(httpQuery)
    ll = httpQueryDic["ll"][0].split(",")
    spn = httpQueryDic["spn"][0].split(",")

    # get center point
    lat = float(ll[0])
    lng = float(ll[1])

    # get span
    s_lat = float(spn[0]) / 2
    s_lng = float(spn[1]) / 2

    return (lat, lng, s_lat, s_lng)

def within_range(request, user):
    lat_min = request.interest_location_lat - request.interest_location_span_lat
    lat_max = request.interest_location_lat + request.interest_location_span_lat
    lng_min = request.interest_location_lng - request.interest_location_span_lng
    lng_max = request.interest_location_lng + request.interest_location_span_lng
    if user.location_lat > lat_min and user.location_lat < lat_max and \
       user.location_lng > lng_min and user.location_lng < lng_max:
        return True
    else:
        return False

def overlap(request1, request2):
    lat_min = request1.interest_location_lat - request1.interest_location_span_lat
    lat_max = request1.interest_location_lat + request1.interest_location_span_lat
    lng_min = request1.interest_location_lng - request1.interest_location_span_lng
    lng_max = request1.interest_location_lng + request1.interest_location_span_lng
    if request2.interest_location_lat > lat_min and request2.interest_location_lat < lat_max and \
       request2.interest_location_lng > lng_min and request2.interest_location_lng < lng_max:
        return True
    else:
        return False

if __name__ == "__main__":
    #print getLocationFromAddress("Pittsburgh")
    print getLocationFromLink("https://maps.google.com/maps?q=Carnegie+Mellon+University,+Pittsburgh,+PA&hl=en&ll=40.443469,-79.943873&spn=0.003523,0.007907&sll=40.431368,-79.9805&sspn=0.225529,0.506058&oq=carne&hq=Carnegie+Mellon+University,+Pittsburgh,+PA&t=m&z=18")
