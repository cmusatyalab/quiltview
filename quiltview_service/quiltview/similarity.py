from gensim import corpora, models, similarities
import sys

usage = "python similarity.py <query>"
if len(sys.argv) != 2 :
	print usage
	exit(0)
else :
	doc = sys.argv[1]
	print "Query: %s" % doc

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

for i in range(0,20) :
	print sims[i] # print sorted (document number, similarity score) 2-tuples
	#print "%s # %s" % (sims[i], documents[int(sims[i][0])])
