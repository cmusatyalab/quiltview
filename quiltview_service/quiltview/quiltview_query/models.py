from django.db import models
from django.utils import timezone

class User(models.Model):
    # status
    is_active = models.BooleanField()
    CONTEXT_CHOICES = (
        ('ID', 'Idle'),
        ('BS', 'Busy'),
        ('DR', 'Driving'),
        ('SL', 'Sleeping'),
    )
    context = models.CharField(max_length = 2, choices = CONTEXT_CHOICES)
    location_lat = models.DecimalField(max_digits = 12, decimal_places = 8)
    location_long = models.DecimalField(max_digits = 12, decimal_places = 8)
    location_update_time = models.DateTimeField()

    # user
    google_account = models.CharField(max_length = 100)
    reputation = models.IntegerField()
    credit = models.IntegerField()

    # preferences
    max_upload_time = models.IntegerField()
    UNIT_CHOICES = (
        ('MM', 'Month'),
        ('DD', 'Day'),
        ('HH', 'Hour'),
        ('MI', 'Minute'),
    )
    max_upload_unit = models.CharField(max_length = 2, choices = UNIT_CHOICES)
    other_preferences = models.CharField(max_length = 255)  # has a risk of exceeding MySQL's max_length of VARCHAR field

    def __unicode__(self):
        return self.google_account

    class Meta:
        db_table = "quiltview_user"

class Query(models.Model):
    # time and location
    requested_time = models.DateTimeField(default = timezone.now())
    latest_upload_time = models.DateTimeField()
    interest_location_lat = models.DecimalField(max_digits = 12, decimal_places = 8)
    interest_location_long = models.DecimalField(max_digits = 12, decimal_places = 8)

    # users
    requester = models.ForeignKey(User)
    #users_asked = models.ManyToManyField(User)
    #users_responded = models.ManyToManyField(User)

    # query content
    content = models.CharField(max_length = 140)
    time_out = models.IntegerField()
    accepted_staleness = models.IntegerField()
    reward = models.IntegerField()

    # responses
    cache_hit = models.BooleanField()
    reload_query = models.BooleanField()
    #video_responses = models.ManyToManyField(Video)

    def __unicode__(self):
        return self.content

    class Meta:
        db_table = "quiltview_query"

class Video(models.Model):
    owner = models.ForeignKey(User)
    query_ID = models.ForeignKey(Query)
    url = models.CharField(max_length = 100)
    upload_time = models.DateTimeField(default = timezone.now())
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
    ACTION_CHOICES = (
        ('Y', 'Replied'),
        ('N', 'Rejected'),
    )
    action = models.CharField(max_length = 2, choices = ACTION_CHOICES)
 
    def __unicode__(self):
        return self.id

    class Meta:
        db_table = "quiltview_prompt" 
