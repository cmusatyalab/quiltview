from quiltview_query.models import User, Query, Video, Prompt

import json

# rendering
from django.template import RequestContext
from django.shortcuts import render_to_response
from django.http import HttpResponse
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


    if request.GET['post']=="True":  # add a new query
        # get lat and lng of the given location
        (lat, lng) = location.getLocationFromAddress(req_query_location)

        # insert a new query
        user = User.objects.all()[0]   # tempory user...
        query = Query(content = "%s at %s?" % (req_query_content, req_query_location),
                      requester = user, 
                      interest_location_lat = lat, 
                      interest_location_long = lng, 
                     )
        query.save()

    # show existing queries
    queries = Query.objects.reverse()
    if queries.count > 10:
        queries = queries[:10]

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

def latest(request):
    req_user_id = request.GET['user_id']
    latest_query = Query.objects.latest('requested_time')
    user = User.objects.get(id = req_user_id)

    # check prompts
    prompts = Prompt.objects.filter(user_id = req_user_id).filter(query_id = latest_query.id)

    response_data = {}
    if prompts.count() == 0:
        prompt = Prompt(user = User.objects.get(id = req_user_id),
                        query = latest_query,
                        requested_time = latest_query.requested_time,
                        user_location_lat = user.location_lat,
                        user_location_long = user.location_long,
                        action = 'W',
                       )
        prompt.save()

        response_data['content'] = latest_query.content
    else:
        pass

    response_data = json.dumps(response_data)
    response = HttpResponse(response_data, content_type="application/json")
    response['Content-Length'] = len(response_data)

    return response
