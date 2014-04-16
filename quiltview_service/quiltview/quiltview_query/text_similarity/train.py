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

'''
This script is used to train models, including tfidf, lda, lsi.
It should in principle work, but has never been tested since the code is reorganized.
One reminder is this script will take more than 10h to run...
Details can be found at http://radimrehurek.com/gensim/. The tutorial session is particularly helpful.
The wiki_en_bow.mm file is generated through 
    python -m gensim.scripts.make_wiki
which is documented here: http://radimrehurek.com/gensim/wiki.html
'''

import sys
import logging, gensim, bz2
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

# load id->word mapping (the dictionary), one of the results of step 2 above
id2word = gensim.corpora.Dictionary.load_from_text('wiki_en_wordids.txt')

# train tfidf
mm = gensim.corpora.MmCorpus('wiki_en_bow.mm')
tfidf = gensim.models.TfidfModel(mm)
tfidf.save('model.tfidf')
mm_tfidf = tfidf[mm]
gensim.corpora.MmCorpus.serialize('wiki_en_my_tfidf.mm', mm_tfidf) # this takes quite a long time

mm_tfidf = gensim.corpora.MmCorpus('wiki_en_my_tfidf.mm')

# train lda
lda = gensim.models.ldamodel.LdaModel(corpus=mm, id2word=id2word, num_topics=100, update_every=1, chunksize=10000, passes=1)
lda.save('model.lda')
#lda.print_topics(100)

# train lda based on tfidf
lda_tfidf = gensim.models.ldamodel.LdaModel(corpus=mm_tfidf, id2word=id2word, num_topics=100, update_every=1, chunksize=10000, passes=1)
lda_tfidf.save('model.lda_tfidf')

# train lsi based on tfidf
lsi_tfidf = gensim.models.lsimodel.LsiModel(corpus=mm_tfidf, id2word=id2word, num_topics=400)
lsi_tfidf.save('model.lsi_tfidf')

#simple test of lda
doc1 = sys.argv[1]
doc2 = sys.argv[2]
doc1_bow = id2word.doc2bow(doc1.lower().split())
doc2_bow = id2word.doc2bow(doc2.lower().split())
doc1_lda = lda[doc1_bow]
doc2_lda = lda[doc2_bow]

print doc1_bow
print doc1_lda

print doc2_bow
print doc2_lda

index = gensim.similarities.MatrixSimilarity([doc1_lda], num_features=100)

sims = index[doc2_lda]

print sims[0]
