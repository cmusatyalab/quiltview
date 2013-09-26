from django.conf.urls import patterns, include, url
from quiltview_query.api import UserResource, QueryResource, VideoResource, PromptResource
from tastypie.api import Api

# Uncomment the next two lines to enable the admin:
# from django.contrib import admin
# admin.autodiscover()

dm = Api(api_name='dm')
dm.register(UserResource())
dm.register(QueryResource())
dm.register(VideoResource())
dm.register(PromtResource())

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'quiltview.views.home', name='home'),
    # url(r'^quiltview/', include('quiltview.foo.urls')),
    url(r'^quiltview/$', 'quiltview_query.views.index'),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    # url(r'^admin/', include(admin.site.urls)),

    (r'^api/', include(dm.urls)),
)
