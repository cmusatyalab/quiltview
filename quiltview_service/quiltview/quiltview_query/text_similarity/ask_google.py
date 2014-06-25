#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys, getopt
import urllib
import simplejson

OPTIONS = ("m:", ["min="])

def print_usage():
    s = "usage: " + sys.argv[0] + " "
    for o in OPTIONS[0]:
        if o != ":" : s += "[-" + o + "] "
    print(s + "query_string\n")

def search(query, index, offset, min_count, quiet=False, rs=[]):
    url = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&rsz=large&%s&start=%s" % (query, offset)
    result = urllib.urlopen(url)
    json = simplejson.loads(result.read())
    status = json["responseStatus"]
    if status == 200:
        results = json["responseData"]["results"]
        cursor = json["responseData"]["cursor"]
        pages = cursor["pages"]
        for r in results:
            i = results.index(r) + (index -1) * len(results) + 1
            u = r["unescapedUrl"]
            rs.append(u)
            if not quiet:
                print("%3d. %s" % (i, u))
        next_index  = None
        next_offset = None
        for p in pages:
            if p["label"] == index:
                i = pages.index(p)
                if i < len(pages) - 1:
                    next_index  = pages[i+1]["label"]
                    next_offset = pages[i+1]["start"]
                break
        if next_index != None and next_offset != None:
            if int(next_offset) < min_count:
                search(query, next_index, next_offset, min_count, quiet, rs)
    return rs

def main():
    min_count = 64
    try:
        opts, args = getopt.getopt(sys.argv[1:], *OPTIONS)
        for opt, arg in opts:
            if opt in ("-m", "--min"):
                min_count = int(arg)
        assert len(args) > 0
    except:
        print_usage()
        sys.exit(1)
    qs = " ".join(args)
    query = urllib.urlencode({"q" : qs})
    search(query, 1, "0", min_count)

if __name__ == "__main__":
    main()
