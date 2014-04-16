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

import sys
import gensim
import re
import os

from django.conf import settings

MODEL_DIR = os.path.join(settings.DJANGO_ROOT, "quiltview_query", "text_similarity")

id2word = gensim.corpora.Dictionary.load_from_text(os.path.join(MODEL_DIR, 'wiki_en_wordids.txt'))
lda = gensim.models.LdaModel.load(os.path.join(MODEL_DIR, 'model.lda'))

def calc_similarity(doc1, doc2):
    # load id->word mapping (the dictionary), one of the results of step 2 above
    doc1_split = re.findall(r"[\w]+|[^\s\w]", doc1.lower())
    doc2_split = re.findall(r"[\w]+|[^\s\w]", doc2.lower())
    doc1_bow = id2word.doc2bow(doc1_split)
    doc2_bow = id2word.doc2bow(doc2_split)
    doc1_lda = lda[doc1_bow]
    doc2_lda = lda[doc2_bow]

    index = gensim.similarities.MatrixSimilarity([doc1_lda], num_features=100)

    sims = index[doc2_lda]

    return sims[0]
