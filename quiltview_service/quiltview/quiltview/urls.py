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
    url(r'^response/$', 'quiltview_query.views.response'),
    url(r'^latest/$', 'quiltview_query.views.latest'),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    url(r'^admin/', include(admin.site.urls)),

    (r'^api/', include(dm.urls)),
)
