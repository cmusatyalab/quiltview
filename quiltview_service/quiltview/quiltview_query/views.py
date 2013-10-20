from quiltview_query.models import User, Query, Video, Prompt
from quiltview_query.forms import QueryForm

import json
import requests
import datetime
import random
import Image

# rendering
from django.template import RequestContext
from django.shortcuts import render_to_response
from django.http import HttpResponse, HttpResponseRedirect
from django.utils import timezone
from django.contrib.auth import logout

import location
from text_similarity import similarity

query_deliver = [[], 0, False]
TMP_IMAGE_0 = "/home/ubuntu/quiltview/quiltview_service/quiltview/STATIC_DIRS/query_images/query_0.jpg"
TMP_IMAGE_PRE = "/home/ubuntu/quiltview/quiltview_service/quiltview/STATIC_DIRS/query_images/query_"


def index(request):
    form = QueryForm()
    return render_to_response('quiltview/query.html',
        {'form':form,
        }, RequestContext(request))

def logout_view(request):
    logout(request)

def query(request):
    def calc_deliver():
        users = User.objects.filter(location_lat__gte = query.interest_location_lat - query.interest_location_span_lat)\
                            .filter(location_lat__lte = query.interest_location_lat + query.interest_location_span_lat)\
                            .filter(location_lng__gte = query.interest_location_lng - query.interest_location_span_lng)\
                            .filter(location_lng__lte = query.interest_location_lng + query.interest_location_span_lng)\
                            .filter(location_update_time__gte = timezone.now() - datetime.timedelta(seconds = 1 * 60))
        for user in users:
            print user.id
        users_to_deliver = []
        counter = 0
        x = range(users.count())
        random.shuffle(x)
        for idx in xrange(users.count()):
            prompts = users[x[idx]].prompt_set.filter(requested_time__gte = timezone.now() - datetime.timedelta(days = 1))
            if prompts.count() >= users[x[idx]].max_upload_time:
                print "prompt%d" % users[x[idx]].id
                continue
            if users[x[idx]].other_preferences:
                preferences = json.loads(users[x[idx]].other_preferences)
            else:
                preferences = {}
            if preferences.get('min_reward', -1) > query.reward:
                print "award%d" % users[x[idx]].id
                continue
            users_to_deliver.append(users[x[idx]].id)
            counter += 1
            if counter >= 3:
                break
        f = request.FILES.get('upload_file', None)
        if f:
            is_file_uploaded = True
        else:
            is_file_uploaded = False
        return (users_to_deliver, query.id, is_file_uploaded)

    def handle_uploaded_file(f):
        if f:
            with open(TMP_IMAGE_0, 'wb') as dest:
                dest.write(f.read())
            img = Image.open(TMP_IMAGE_0)
            img = img.resize((250, 250), Image.ANTIALIAS) 
            img.save(TMP_IMAGE_PRE + "%d.jpg" % query.id)
            query.is_query_image = True
            query.save()

    global query_deliver

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
            return render_to_response('quiltview/query.html', 
                {'is_login_error':True, 'error_message':"Error: You have to sign in before posting a query.", 'form':form}, 
                RequestContext(request))

        # get lat and lng of the given location
        #(lat, lng) = location.getLocationFromAddress(req_query_location)
        (lat, lng, s_lat, s_lng) = location.getLocationFromLink(req_query_location)

        # insert a new query
        try:
            user = User.objects.get(google_account = user_email)
        except:
            return render_to_response('quiltview/query.html', 
                {'is_error':True, 'error_message':"Error: The current logged in user has not been registered. Please contact the administrator.", 'form':form}, 
                RequestContext(request))
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
        if req_reward:
            query.reward = int(req_reward)

        # check cache based on time
        matched_queries = Query.objects.filter(requested_time__gte = query.requested_time - datetime.timedelta(seconds = query.accepted_staleness))\
                                       .filter(interest_location_lat = lat).filter(interest_location_lng = lng).filter(interest_location_span_lat = s_lat).filter(interest_location_span_lng = s_lng)
        # detect similarity
        closest_queries = []
        for matched_query in matched_queries.all():
            if similarity.calc_similarity(matched_query.content, query.content) > 0.3 and location.overlap(matched_query, query):
                closest_queries.append(matched_query)

        if len(closest_queries) > 0:   # cache hit
            query.cache_hit = True
            req_reload = request.POST.get('reload', 'False')
            if req_reload == "True":
                query.reload_query = True

        query.save()

        # handle uploaed file
        handle_uploaded_file(request.FILES.get('upload_file', None))

        if query.cache_hit and (not query.reload_query):    # cache hit
            print "Cache hit!!!"
            # prepare parameters to reload
            parameter_string = "query_id=%d" % query.id
            return render_to_response('quiltview/query.html',
                {'queries':closest_queries, 'is_cache':True, 'parameter':parameter_string, 'form':form,
                }, RequestContext(request))
        else:     # not cached or reloaded
            # calculate 1. a pool of users that should deliver the message 2. query id 3. if a file is uploaded
            query_deliver = calc_deliver()
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

def reload(request):
    def calc_deliver():
        users = User.objects.filter(location_lat__gte = query.interest_location_lat - query.interest_location_span_lat)\
                            .filter(location_lat__lte = query.interest_location_lat + query.interest_location_span_lat)\
                            .filter(location_lng__gte = query.interest_location_lng - query.interest_location_span_lng)\
                            .filter(location_lng__lte = query.interest_location_lng + query.interest_location_span_lng)\
                            .filter(location_update_time__gte = timezone.now() - datetime.timedelta(seconds = 1 * 60))
        for user in users:
            print user.id
        users_to_deliver = []
        counter = 0
        x = range(users.count())
        random.shuffle(x)
        for idx in xrange(users.count()):
            prompts = users[x[idx]].prompt_set.filter(requested_time__gte = timezone.now() - datetime.timedelta(days = 1))
            if prompts.count() >= users[x[idx]].max_upload_time:
                continue
            if users[x[idx]].other_preferences:
                preferences = json.loads(users[x[idx]].other_preferences)
            else:
                preferences = {}
            if preferences.get('min_reward', -1) > query.reward:
                continue
            users_to_deliver.append(users[x[idx]].id)
            counter += 1
            if counter >= 3:
                break
        is_file_uploaded = query.is_query_image
        return (users_to_deliver, query.id, is_file_uploaded)

    global query_deliver

    form = QueryForm()
    req_query_id = int(request.GET['query_id'])
    query = Query.objects.get(id=req_query_id)
    old_id = query.id
    query.pk = None
    query.reload_query = True
    query.save()

    if query.is_query_image:
        img = Image.open(TMP_IMAGE_PRE + "%d.jpg" % old_id)
        img.save(TMP_IMAGE_PRE + "%d.jpg" % query.id)
    
    query_deliver = calc_deliver()
    return render_to_response('quiltview/query.html',
        {'query':query, 'is_post':True, 'form':form,
        }, RequestContext(request))

def response(request):
    req_query_id = int(request.GET['query_id'])
    query = Query.objects.get(id=req_query_id)
    responses = query.video_set.all()

    return render_to_response('quiltview/response.html',
        {'responses':responses, 'query':query,
        }, RequestContext(request))

def latest(request):
    global query_deliver
    print query_deliver

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
          latest_query.id == query_deliver[1] and \
          user.id in query_deliver[0]:
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
        if query_deliver[2]:
            response_data['image'] = "http://quiltview.opencloudlet.org/static/query_images/query_%d.jpg" % latest_query.id
        else:
            response_data['image'] = ""
    else:
        pass

    response_data = json.dumps(response_data)
    response = HttpResponse(response_data, content_type="application/json")
    response['Content-Length'] = len(response_data)

    return response
