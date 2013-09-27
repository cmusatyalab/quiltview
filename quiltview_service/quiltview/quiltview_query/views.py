from quiltview_query.models import User, Query, Video, Prompt

# rendering
from django.template import RequestContext
from django.shortcuts import render_to_response
from django.utils import timezone

import location

def index(request):
    return render_to_response('quiltview/index.html', {}, RequestContext(request))

def query(request):
    req_query_content = request.GET['query_content']
    req_query_location = request.GET['query_location']
    req_time_out_n = request.GET['time_out_n']
    req_accepted_staleness_n = request.GET['accepted_staleness_n']
    req_reward = request.GET['reward']

    # get lat and lng of the given location
    (lat, lng) = location.getLocationFromAddress(req_query_location)

    # insert a new query
    user = User.objects.all()[0]
    query = Query(content = "%s at %s?" % (req_query_content, req_query_location),
                  requester = user, 
                  latest_upload_time = "2013-09-26", 
                  interest_location_lat = lat, 
                  interest_location_long = lng, 
                  time_out = int(req_time_out_n), 
                  accepted_staleness = int(req_accepted_staleness_n), 
                  reward= int(req_reward),
                 )
    query.save()

    # show existing queries
    queries = Query.objects.order_by('requested_time')

    return render_to_response('quiltview/index.html',
        {'queries':queries, 'is_query':True,
        }, RequestContext(request))

def response(request):
    req_query_id = int(request.GET['query_id'])
    query = Query.objects.get(id=req_query_id)
    responses = query.video_set.all()

    return render_to_response('quiltview/response.html',
        {'responses':responses, 'query':query,
        }, RequestContext(request))
