from gensim import corpora, models, similarities
from pprint import pprint

import sys

def learn(new_documents):
    with open("/home/ubuntu/quiltview/quiltview_service/quiltview/quiltview_query/text_similarity/documents") as f:
        documents = f.readlines()
    documents = new_documents + documents

    for document in documents:
        document = document.lower()

    # remove common words and tokenize
    stoplist = set('there a the is what how it at in on'.split())#'for a of the and to in'.split())
    texts = [[word for word in document.lower().split() if word not in stoplist]
             for document in documents]
    # remove words that appear only once
    all_tokens = sum(texts, [])
    #print all_tokens
    tokens_once = set(word for word in set(all_tokens) if all_tokens.count(word) == 1)
    texts = [[word for word in text if word not in tokens_once]
             for text in texts]
    #pprint(texts)

    dictionary = corpora.Dictionary(texts)
    dictionary.save('/tmp/deerwester.dict') # store the dictionary, for future reference
    #print dictionary

    corpus = [dictionary.doc2bow(text) for text in texts]
    corpora.MmCorpus.serialize('/tmp/deerwester.mm', corpus) # store to disk, for later use
    #print corpus

