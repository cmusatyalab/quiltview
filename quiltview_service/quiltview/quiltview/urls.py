#!/usr/bin/env python 
#
# QuiltView: a Crowd-Sourced Video Response System
#
#   Author: Zhuo Chen <zhuoc@cs.cmu.edu>
#
#   Copyright (C) 2011-2013 Carnegie Mellon University
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

from django.conf.urls import patterns, include, url
from quiltview_query.api import UserResource, QueryResource, VideoResource, PromptResource
from tastypie.api import Api
#from django.views.generic import TemplateView

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

dm = Api(api_name='dm')
dm.register(UserResource())
dm.register(QueryResource())
dm.register(VideoResource())
dm.register(PromptResource())

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'quiltview.views.home', name='home'),
    # url(r'^quiltview/', include('quiltview.foo.urls')),
    (r'^browserid/', include('django_browserid.urls')),

    #url(r'^$', 'quiltview_query.views.login'),
    url(r'^$', 'quiltview_query.views.index'),
    #url(r'^logout/$', 'quiltview_query.views.logout'),
    url(r'^logout/$', 'django.contrib.auth.views.logout', {'next_page': 'http://quiltview.opencloudlet.org'}),
    url(r'^query/$', 'quiltview_query.views.query'),
    url(r'^reload/$', 'quiltview_query.views.reload'),
    url(r'^response/$', 'quiltview_query.views.response'),
    url(r'^latest/$', 'quiltview_query.views.latest'),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    url(r'^admin/', include(admin.site.urls)),

    (r'^api/', include(dm.urls)),
)
