import sys
import gensim
import re
import os

from django.conf import settings

MODEL_DIR = os.path.join(settings.DJANGO_ROOT, "quiltview_query", "text_similarity")

id2word = gensim.corpora.Dictionary.load_from_text(MODEL_DIR + 'wiki_en_wordids.txt')
lda = gensim.models.LdaModel.load(MODEL_DIR + 'model.lda')

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
