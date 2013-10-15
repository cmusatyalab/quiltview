import sys
import logging, gensim, bz2
#logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

MODEL_DIR = "/home/ubuntu/quiltview/quiltview_service/quiltview/quiltview_query/text_similarity/"

id2word = gensim.corpora.Dictionary.load_from_text(MODEL_DIR + 'wiki_en_wordids.txt')
lda = gensim.models.LdaModel.load(MODEL_DIR + 'model.lda')

def calc_similarity(doc1, doc2):
    # load id->word mapping (the dictionary), one of the results of step 2 above
    doc1_bow = id2word.doc2bow(doc1.lower().split())
    doc2_bow = id2word.doc2bow(doc2.lower().split())
    doc1_lda = lda[doc1_bow]
    doc2_lda = lda[doc2_bow]

    index = gensim.similarities.MatrixSimilarity([doc1_lda], num_features=100)

    sims = index[doc2_lda]

    return sims[0]
