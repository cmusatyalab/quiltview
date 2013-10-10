from quiltview_query.models import User, Query, Video, Prompt
from quiltview_query.forms import QueryForm

import json
import requests
import datetime
import random

# rendering
from django.template import RequestContext
from django.shortcuts import render_to_response
from django.http import HttpResponse, HttpResponseRedirect
from django.utils import timezone
from django.contrib.auth import logout

import location
from text_similarity import similarity, learn_dictionary

users_to_deliver = []

def index(request):
    form = QueryForm()
    return render_to_response('quiltview/query.html',
        {'form':form,
        }, RequestContext(request))

def logout_view(request):
    logout(request)

def query(request):
    def calc_users_deliver():
        users = User.objects.filter(location_lat__gte = query.interest_location_lat - query.interest_location_span_lat)\
                            .filter(location_lat__lte = query.interest_location_lat + query.interest_location_span_lat)\
                            .filter(location_lng__gte = query.interest_location_lng - query.interest_location_span_lng)\
                            .filter(location_lng__lte = query.interest_location_lng + query.interest_location_span_lng)\
                            .filter(location_update_time__gte = timezone.now() - datetime.timedelta(seconds = 1 * 60))
        users_to_deliver = []
        counter = 0
        x = range(users.count())
        random.shuffle(x)
        for idx in xrange(users.count()):
            prompts = users[x[idx]].prompt_set.filter(requested_time__gte = timezone.now() - datetime.timedelta(days = 1))
            if prompts.count() < users[x[idx]].max_upload_time:
                users_to_deliver.append(users[x[idx]].id)
                counter += 1
                if counter >= 3:
                    break
        return users_to_deliver
    def handle_uploaded_file(f):
        if f:
            destination = open('/home/ubuntu/test.jpg', 'wb')
            destination.write(f.read())
            destination.close()
        else:
            print 10000000
        
    global users_to_deliver

    '''
    if request.method == 'POST':
        form = QueryForm(request.POST)
        if form.is_valid():
            req_query_content = form.cleaned_data['query_content']
            print req_query_content
            req_query_location = form.cleaned_data['query_location']
            req_time_out_n = form.cleaned_data['time_out_n']
            if req_time_out_n:
                req_time_out = int(req_time_out_n) * 60
            req_accepted_staleness_n = form.cleaned_data['accepted_staleness_n']
            if req_accepted_staleness_n:
                req_accepted_staleness = int(req_accepted_staleness_n) * 60
            req_reward = form.cleaned_data['reward']
            req_expected_reply = form.cleaned_data['expected_reply']

            if request.POST['post']=="True": # add a new query
                pass
            else:
                
            return render_to_response('quiltview/query.html',
                {'form':form,
                }, RequestContext(request))
    else:
        form = QueryForm()

    return render_to_response('quiltview/query.html',
        {'form':form,
        }, RequestContext(request))
    '''

    form = QueryForm(request.POST, request.FILES)
    req_query_content = request.POST['query_content']
    req_query_location = request.POST['query_location']
    req_time_out_n = request.POST['time_out_n']
    if req_time_out_n:
        req_time_out = int(req_time_out_n) * 60
    req_accepted_staleness_n = request.POST['accepted_staleness_n']
    if req_accepted_staleness_n:
        req_accepted_staleness = int(req_accepted_staleness_n) * 60
    req_reward = request.POST['reward']
    req_expected_reply = request.POST['expected_reply']

    if request.POST['post']=="True":  # add a new query
        # check if the user has logged in
        user_email = request.POST['user_email']
        if not user_email:
            return render_to_response('quiltview/query.html', {'is_login_error':True, 'form':form}, RequestContext(request))

        # handle file upload
        handle_uploaded_file(request.FILES.get('upload_file', None))

        # get lat and lng of the given location
        #(lat, lng) = location.getLocationFromAddress(req_query_location)
        (lat, lng, s_lat, s_lng) = location.getLocationFromLink(req_query_location)

        # insert a new query
        user = User.objects.get(google_account = user_email)
        query = Query(content = "%s" % (req_query_content),
                      requester = user,
                      interest_location_lat = lat,
                      interest_location_lng = lng,
                      interest_location_span_lat = s_lat,
                      interest_location_span_lng = s_lng,
                      requested_time = timezone.now()
                     )
        if req_time_out_n:
            query.time_out = req_time_out
        if req_accepted_staleness_n:
            query.accepted_staleness = req_accepted_staleness

        # check cache based on time
        matched_queries = Query.objects.filter(requested_time__gte = query.requested_time - datetime.timedelta(seconds = query.accepted_staleness))\
                                       .filter(interest_location_lat = lat).filter(interest_location_lng = lng).filter(interest_location_span_lat = s_lat).filter(interest_location_span_lng = s_lng)
        # detect similarity
        new_documents = []
        for matched_query in matched_queries.all():
            new_documents.append(matched_query.content)
        learn_dictionary.learn(new_documents)
        closest_query_idxes = similarity.find_closest(query.content, len(new_documents))
        closest_queries = []
        for idx in closest_query_idxes:
            if location.overlap(matched_queries[idx], query):
                closest_queries.append(matched_queries[idx])

        if len(closest_queries) > 0:   # cache hit
            query.cache_hit = True
            req_reload = request.POST.get('reload', 'False')
            if req_reload == "True":
                query.reload_query = True

        if query.cache_hit and (not query.reload_query):    # cache hit
            print "Cache hit!!!"
            # prepare parameters to reload
            req_query_location_POST = req_query_location.replace(':', '%3A').replace('/', '%2F').replace('?', '%3F').replace('=', '%3D').replace('+', '%2B').replace(',', '%2C').replace('&', '%26')
            parameter_string = "query_content=%s&query_location=%s&time_out_n=%s&accepted_staleness_n=%s&reward=%s&reload=True&post=True&user_email=%s" % (req_query_content, 
                req_query_location_POST, req_time_out_n, req_accepted_staleness_n, req_reward, user_email)
            query.save()
            return render_to_response('quiltview/query.html',
                {'queries':closest_queries, 'is_cache':True, 'parameter':parameter_string, 'form':form,
                }, RequestContext(request))
        else:     # not cached or reloaded
            # calculate a pool of users that should deliver the message
            users_to_deliver = calc_users_deliver()
            query.save()
            return render_to_response('quiltview/query.html',
                {'query':query, 'is_post':True, 'form':form,
                }, RequestContext(request))
    else:
        # show existing queries
        queries = Query.objects.order_by('requested_time').reverse()
        query_count = queries.count
        if queries.count > 10:
            queries = queries[:10]

        return render_to_response('quiltview/query.html',
            {'queries':queries, 'query_count':query_count, 'is_check':True, 'form':form,
            }, RequestContext(request))

def response(request):
    req_query_id = int(request.GET['query_id'])
    query = Query.objects.get(id=req_query_id)
    responses = query.video_set.all()

    return render_to_response('quiltview/response.html',
        {'responses':responses, 'query':query,
        }, RequestContext(request))

def latest(request):
    global users_to_deliver
    print users_to_deliver

    req_user_id = request.GET['user_id']
    #print req_user_id
    req_user_lat = request.GET['lat']
    req_user_lng = request.GET['lng']

    response_data = {}

    user = User.objects.get(uuid = req_user_id)
    user.location_lat = float(req_user_lat)
    user.location_lng = float(req_user_lng)
    user.location_update_time = timezone.now()
    user.save()
    
    if Query.objects.count() == 0:  #empty query database
        response_data = json.dumps(response_data)
        response = HttpResponse(response_data, content_type="application/json")
        response['Content-Length'] = len(response_data)
        return response

    latest_query = Query.objects.latest('requested_time')

    prompts = Prompt.objects.filter(user_id = user.id).filter(query_id = latest_query.id)

    response_data = {}
    # check prompts, cache and location
    if prompts.count() == 0 and \
          (not (latest_query.cache_hit and (not latest_query.reload_query))) and \
          user.id in users_to_deliver:
        prompt = Prompt(user = user,
                        query = latest_query,
                        requested_time = latest_query.requested_time,
                        user_location_lat = user.location_lat,
                        user_location_lng = user.location_lng,
                        action = 'W',
                       )
        prompt.save()

        response_data['user_id'] = user.id
        response_data['content'] = latest_query.content
        response_data['query_id'] = latest_query.id
    else:
        pass

    response_data = json.dumps(response_data)
    response = HttpResponse(response_data, content_type="application/json")
    response['Content-Length'] = len(response_data)

    return response
