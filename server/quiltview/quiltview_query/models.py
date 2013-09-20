from django.db import models
from django.utils import timezone
import my_configure

class Query(models.Model):
    # time and location
    requested_time = models.DateTimeField(default = timezone.now())
    latest_upload_time = models.DateTimeField()
    interest_location_lat = models.DecimalField(max_digits = 12, decimal_places = 8)
    interest_location_long = models.DecimalField(max_digits = 12, decimal_places = 8)

    # users
    requester = models.ForeignKey(User)
    users_asked = models.ManyToManyField(User)
    users_responded = models.ManyToManyField(User)

    # query content
    content = models.CharField(max_length = 140)
    time_out = models.IntegerField()
    back_time_allowance = models.IntegerField()
    reward = models.IntegerField()

    # responses
    cache_hit = models.BooleanField()
    reload_query = models.BooleanField()
    video_responses = models.ManyToManyField(Video)

    def __unicode__(self):
        return self.content

    class Meta:
        db_table = "quiltview_query"

class User(models.Model):
    # status
    is_active = models.BooleanField()
    context = models.IntegerField(default = my_configure.CONTEXT_IDLE)
    location_lat = models.DecimalField(max_digits = 12, decimal_places = 8)
    location_long = models.DecimalField(max_digits = 12, decimal_places = 8)

    # user
    google_account = models.CharField(max_length = 100)
    reputation = models.IntegerField()
    credit = models.IntegerField()

    # preferences
    max_upload_time = models.IntegerField()
    max_upload_unit = models.IntegerField()   #ENUM
    other_preferences = models.CharField(max_length = 255)  # has a risk of exceeding MySQL's max_length of VARCHAR field

    def __unicode__(self):
        return self.google_account

    class Meta:
        db_table = "quiltview_user"

class Video(models.Model):
    owner = models.ForeignKey(User)
    query_ID = models.ForeignKey(Query)
    url = models.CharField(max_length = 100)
    upload_time = models.DateTimeField()
    upload_location_lat = models.DecimalField(max_digits = 12, decimal_places = 8)
    upload_location_long = models.DecimalField(max_digits = 12, decimal_places = 8)

    def __unicode__(self):
        return self.url

    class Meta:
        db_table = "quiltview_video"
    
class Prompt(models.Model):
    user = models.ForeignKey(User)
    query_ID = models.ForeignKey(Query)
    video_ID = models.ForeignKey(Video)

    requested_time = models.DateTimeField()
    responded_time = models.DateTimeField()
    user_location_lat = models.DecimalField(max_digits = 12, decimal_places = 8)
    user_location_long = models.DecimalField(max_digits = 12, decimal_places = 8)
    action = models.IntegerField()  #ENUM
 
    def __unicode__(self):
        return self.id

    class Meta:
        db_table = "quiltview_promt" 
