from django.db import models
from django.utils import timezone

class User(models.Model):
    # status
    is_active = models.BooleanField(default = True)
    CONTEXT_CHOICES = (
        ('ID', 'Idle'),
        ('BS', 'Busy'),
        ('DR', 'Driving'),
        ('SL', 'Sleeping'),
    )
    context = models.CharField(max_length = 2, choices = CONTEXT_CHOICES, default = 'ID')
    location_lat = models.DecimalField(max_digits = 12, decimal_places = 8)
    location_lng = models.DecimalField(max_digits = 12, decimal_places = 8)
    location_update_time = models.DateTimeField(default = timezone.now())

    # user
    google_account = models.CharField(max_length = 100, null=False, blank=False)
    uuid = models.CharField(max_length = 50, null=False, blank=False)
    reputation = models.IntegerField(default = 90)
    credit = models.IntegerField(default = 3)

    # preferences
    max_upload_time = models.IntegerField(default = 500)
    UNIT_CHOICES = (
        ('MM', 'Month'),
        ('DD', 'Day'),
        ('HH', 'Hour'),
        ('MI', 'Minute'),
    )
    max_upload_unit = models.CharField(max_length = 2, choices = UNIT_CHOICES, default = 'DD')
    other_preferences = models.CharField(max_length = 255, null=True, blank=True)  # has a risk of exceeding MySQL's max_length of VARCHAR field

    def __unicode__(self):
        return unicode(self.google_account)

    def save(self, *args, **kwargs):
         self.location_update_time = timezone.now()
         return super(User, self).save(*args, **kwargs)

    class Meta:
        db_table = "quiltview_user"

class Query(models.Model):
    # time and location
    requested_time = models.DateTimeField()
    # TODO: this is not updated now...
    latest_upload_time = models.DateTimeField(null=True, blank=True)
    interest_location_lat = models.DecimalField(max_digits = 12, decimal_places = 8)
    interest_location_lng = models.DecimalField(max_digits = 12, decimal_places = 8)
    interest_location_span_lat = models.DecimalField(max_digits = 12, decimal_places = 8)
    interest_location_span_lng = models.DecimalField(max_digits = 12, decimal_places = 8)

    # users
    requester = models.ForeignKey(User)
    #users_asked = models.ManyToManyField(User)
    #users_responded = models.ManyToManyField(User)

    # query content
    content = models.CharField(max_length = 140)
    time_out = models.IntegerField(default = 10 * 60) # 10 mins
    accepted_staleness = models.IntegerField(default = 10 * 60) # 10 mins
    reward = models.IntegerField(default = 1)
    expected_reply = models.IntegerField(default = 3)
    is_query_image = models.BooleanField(default = False)

    # responses
    cache_hit = models.BooleanField(default = False)
    reload_query = models.BooleanField(default = False)
    #video_responses = models.ManyToManyField(Video)

    def __unicode__(self):
        return self.content

    class Meta:
        db_table = "quiltview_query"

class Video(models.Model):
    owner = models.ForeignKey(User)
    query = models.ForeignKey(Query)
    url = models.CharField(max_length = 100)
    upload_time = models.DateTimeField(default = timezone.now())
    upload_location_lat = models.DecimalField(max_digits = 12, decimal_places = 8)
    upload_location_lng = models.DecimalField(max_digits = 12, decimal_places = 8)

    def __unicode__(self):
        return self.url

    def save(self, *args, **kwargs):
         self.upload_time = timezone.now()
         return super(Video, self).save(*args, **kwargs)

    class Meta:
        db_table = "quiltview_video"
    
class Prompt(models.Model):
    user = models.ForeignKey(User)
    query = models.ForeignKey(Query)
    video = models.ForeignKey(Video, null=True, blank=True)

    requested_time = models.DateTimeField()
    responded_time = models.DateTimeField(null=True, blank=True)
    user_location_lat = models.DecimalField(max_digits = 12, decimal_places = 8)
    user_location_lng = models.DecimalField(max_digits = 12, decimal_places = 8)
    ACTION_CHOICES = (
        ('Y', 'Replied'),
        ('N', 'Rejected'),
        ('W', 'Waiting'),
    )
    action = models.CharField(max_length = 2, choices = ACTION_CHOICES)
 
    def __unicode__(self):
        return self.id

    class Meta:
        db_table = "quiltview_prompt" 
