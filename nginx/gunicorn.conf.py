bind = '127.0.0.1:8020'
workers = 8
worker_class = 'gevent'
pidfile = 'gunicorn.pid'
proc_name = 'quiltview'
secure_scheme_headers = {'X-FORWARDED-PROTOCOL': 'https'}
timeout = 300
