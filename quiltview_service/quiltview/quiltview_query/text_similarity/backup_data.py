#!/usr/bin/env python

import os
import boto
import boto.s3.connection

BUCKET_NAME = 'text_similarity_model'

access_key = os.environ.get('ACCESS_KEY')
secret_key = os.environ.get('SECRET_KEY')
print access_key
print secret_key

conn = boto.connect_s3(
        aws_access_key_id = access_key,
        aws_secret_access_key = secret_key,
        host = 'storage.cmusatyalab.org',
        is_secure = True,
        calling_format = boto.s3.connection.OrdinaryCallingFormat(),
        )

try:
    bucket = conn.get_bucket(BUCKET_NAME)
except:
    bucket = conn.create_bucket(BUCKET_NAME)

key = bucket.new_key('wiki_en_wordids.txt')
key.set_contents_from_filename("wiki_en_wordids.txt")
key.set_acl('public-read')
url = key.generate_url(expires_in=0, query_auth=False)
print url

key = bucket.new_key('model.lda')
key.set_contents_from_filename("model.lda")
key.set_acl('public-read')
url = key.generate_url(expires_in=0, query_auth=False)
print url
