import requests

GOOGLE_LOCATION_API = "http://maps.googleapis.com/maps/api/geocode/json"

def getLocationFromAddress(address):
    url = GOOGLE_LOCATION_API + "?address=%s&sensor=false" % address
    print("Connecting to %s" % (url))

    r = requests.get(url)
    json_result = r.json()
    location = json_result['results'][0]['geometry']['location']

    return (location['lat'], location['lng'])

def getLocationFromLink(link):
    # get center point
    ll_start = link.find("&ll=") + 4
    ll_end = link.find("&spn=")
    ll = link[ll_start : ll_end]
    lat, lng = ll.split(',')
    lat = float(lat)
    lng = float(lng)

    # get span
    span_start = ll_end + 5
    span_end = link.find("&sll=")
    span = link[span_start : span_end]
    s_lat, s_lng = span.split(',')
    s_lat = float(s_lat) / 2
    s_lng = float(s_lng) / 2

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
        print (lat_min, lat_max, user.location_lat)
        print (lng_min, lng_max, user.location_lng)
        return False

if __name__ == "__main__":
    #print getLocationFromAddress("Pittsburgh")
    print getLocationFromLink("https://maps.google.com/maps?q=Carnegie+Mellon+University,+Pittsburgh,+PA&hl=en&ll=40.443469,-79.943873&spn=0.003523,0.007907&sll=40.431368,-79.9805&sspn=0.225529,0.506058&oq=carne&hq=Carnegie+Mellon+University,+Pittsburgh,+PA&t=m&z=18")
