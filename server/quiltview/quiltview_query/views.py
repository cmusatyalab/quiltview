from quiltview_query.models import User, Query, Video, Prompt

# rendering
from django.template import loader, RequestContext
from django.http import HttpResponse
from django.shortcuts import render_to_response, get_object_or_404

import datetime
from django.utils import timezone


def index(request):
    c = RequestContext(request, {
    })
    return render_to_response('quiltview/index.html', {}, RequestContext(request))

