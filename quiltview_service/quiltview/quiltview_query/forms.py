from django import forms

class QueryForm(forms.Form):
    query_content = forms.CharField(max_length = 140)
    query_location = forms.CharField(max_length = 255)

    UNIT_CHOICES = (
        ('MI', 'Minute'),
        ('HH', 'Hour'),
        ('DD', 'Day'),
    )

    time_out_n = forms.IntegerField()
    time_out_unit = forms.ChoiceField(UNIT_CHOICES)
    accepted_staleness_n = forms.IntegerField()
    accepted_staleness_unit = forms.ChoiceField(UNIT_CHOICES)
    reward = forms.IntegerField()
    expected_reply = forms.IntegerField()

    upload_file = forms.FileField()
