from django import forms

class QueryForm(forms.Form):
    query_content = forms.CharField(max_length = 140, widget=forms.TextInput(attrs={'placeholder': 'e.g. What is the weather'}))
    query_location = forms.CharField(max_length = 255, widget=forms.TextInput(attrs={'placeholder': 'put Google Maps link here'}))

    UNIT_CHOICES = (
        ('MI', 'Minute'),
        ('HH', 'Hour'),
        ('DD', 'Day'),
    )

    time_out_n = forms.IntegerField(widget=forms.TextInput(attrs={'placeholder': '10'}))
    time_out_unit = forms.ChoiceField(UNIT_CHOICES)
    accepted_staleness_n = forms.IntegerField(widget=forms.TextInput(attrs={'placeholder': '10'}))
    accepted_staleness_unit = forms.ChoiceField(UNIT_CHOICES)
    reward = forms.IntegerField(widget=forms.TextInput(attrs={'placeholder': '1'}))
    expected_reply = forms.IntegerField(widget=forms.TextInput(attrs={'placeholder': '3'}))

    upload_file = forms.FileField()
