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

# The url of the server you want to contact
QUILTVIEW_URL = "http://quiltview.opencloudlet.org"

# The resource url on the quiltview server to post new video
# You probably don't need to change this
VIDEO_RESOURCE = "/api/dm/video/"

# The temporary video name
TMP_VIDEO_NAME = "uploaded_video"

# Wheter upload to youtube or just store locally
IS_UPLOAD_YOUTUBE = False
