from gensim import corpora, models, similarities
import sys

def find_closest(doc, new_documents_len):
    dictionary = corpora.Dictionary.load('/tmp/deerwester.dict')
    corpus = corpora.MmCorpus('/tmp/deerwester.mm') # comes from the first tutorial, "From strings to vectors"
    #print corpus

    lsi = models.LsiModel(corpus, id2word=dictionary, num_topics=2)
    #doc = "Has the murderer been caught" # query!!!
    vec_bow = dictionary.doc2bow(doc.lower().split())
    vec_lsi = lsi[vec_bow] # convert the query to LSI space
    #print vec_lsi

    index = similarities.MatrixSimilarity(lsi[corpus]) # transform corpus to LSI space and index it

    index.save('/tmp/deerwester.index')
    index = similarities.MatrixSimilarity.load('/tmp/deerwester.index')

    sims = index[vec_lsi] # perform a similarity query against the corpus
    #print list(enumerate(sims)) # print (document_number, document_similarity) 2-tuples

    sims = sorted(enumerate(sims), key=lambda item: -item[1])

    results = []
    for i in range(len(sims)) :
        #print sims[i] # print sorted (document number, similarity score) 2-tuples
        #print documents[sims[i][0]].strip()
        if sims[i][1] < 0.99:
            break
        if sims[i][0] < new_documents_len:
            results.append(sims[i][0])
    return results
