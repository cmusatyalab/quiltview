# quiltview/api.py
import os
from tastypie.authorization import Authorization
from tastypie.resources import ModelResource, ALL, ALL_WITH_RELATIONS
#from django.contrib.auth.models import User
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
    requester = fields.ForeignKey(UserResource, 'user')

    class Meta:
        serializer = PrettyJSONSerializer()
        authorization = Authorization()
        queryset = Query.objects.all()
        always_return_data = True
        resource_name = 'query'
        list_allowed_methods = ['get', 'post', 'put']

class VideoResource(ModelResource):
    owner = fields.ForeignKey(UserResource, 'owner')
    query_ID = fields.ForeignKey(QueryResource, 'query_ID')

    class Meta:
        serializer = PrettyJSONSerializer()
        authorization = Authorization()
        queryset = Video.objects.all()
        always_return_data = True
        resource_name = 'video'
        list_allowed_methods = ['get', 'post', 'put']

class PromptResource(ModelResource):
    user = fields.ForeignKey(UserResource, 'user')
    video_ID = fields.ForeignKey(VideoResource, 'video_ID')
    query_ID = fields.ForeignKey(QueryResource, 'query_ID')

    class Meta:
        serializer = PrettyJSONSerializer()
        authorization = Authorization()
        queryset = Prompt.objects.all()
        always_return_data = True
        resource_name = 'prompt'
        list_allowed_methods = ['get', 'post', 'put']

