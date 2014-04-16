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

import os
from tastypie.authorization import Authorization
from tastypie.resources import ModelResource, ALL, ALL_WITH_RELATIONS
#from django.contrib.auth.models import User
from tastypie.cache import NoCache
from tastypie.resources import ModelResource
from tastypie import fields
from quiltview_query.models import User
from quiltview_query.models import Query
from quiltview_query.models import Video
from quiltview_query.models import Prompt

from django.core.serializers import json
from django.utils import simplejson
from tastypie.serializers import Serializer

class PrettyJSONSerializer(Serializer):
    json_indent = 2
    def to_json(self, data, options=None):
        options = options or {}
        data = self.to_simple(data, options)
        return simplejson.dumps(data, cls=json.DjangoJSONEncoder,
                sort_keys=True, ensure_ascii=False, indent=self.json_indent)


class UserResource(ModelResource):
    class Meta:
        serializer = PrettyJSONSerializer()
        authorization = Authorization()
        queryset = User.objects.all()
        always_return_data = True
        resource_name = 'user'
        list_allowed_methods = ['get', 'post', 'put']
 
class QueryResource(ModelResource):
    requester = fields.ForeignKey(UserResource, 'requester')

    class Meta:
        serializer = PrettyJSONSerializer()
        authorization = Authorization()
        queryset = Query.objects.all()
        always_return_data = True
        resource_name = 'query'
        list_allowed_methods = ['get', 'post', 'put']

class VideoResource(ModelResource):
    owner = fields.ForeignKey(UserResource, 'owner')
    query = fields.ForeignKey(QueryResource, 'query')

    class Meta:
        serializer = PrettyJSONSerializer()
        authorization = Authorization()
        queryset = Video.objects.all()
        always_return_data = True
        resource_name = 'video'
        list_allowed_methods = ['get', 'post', 'put']
        cache = NoCache()

class PromptResource(ModelResource):
    user = fields.ForeignKey(UserResource, 'user')
    video = fields.ForeignKey(VideoResource, 'video', null=True)
    query = fields.ForeignKey(QueryResource, 'query')

    class Meta:
        serializer = PrettyJSONSerializer()
        authorization = Authorization()
        queryset = Prompt.objects.all()
        always_return_data = True
        resource_name = 'prompt'
        list_allowed_methods = ['get', 'post', 'put']

