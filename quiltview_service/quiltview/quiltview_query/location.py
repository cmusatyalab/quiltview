import requests

GOOGLE_LOCATION_API = "http://maps.googleapis.com/maps/api/geocode/json"

def getLocationFromAddress(address):
    url = GOOGLE_LOCATION_API + "?address=%s&sensor=false" % address
    print("Connecting to %s" % (url))

    r = requests.get(url)
    json_result = r.json()
    location = json_result['results'][0]['geometry']['location']

    return (location['lat'], location['lng'])

if __name__ == "__main__":
    print getLocationFromAddress("Pittsburgh")
