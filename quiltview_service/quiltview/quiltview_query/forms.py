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
