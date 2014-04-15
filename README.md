# QuiltView: a Crowd-Sourced Video Response System

## What is QuiltView?

QuiltView is a crowd-sourced video response system based on smart glasses such as [Google Glass](http://www.google.com/glass). Specifically, it leverages the ability of capturing first-person view-point video with effortless one-touch in such Glass devices. The extreme simplicity of video capture can be used to create a new near-real-time social network. In this network, users can pose brief queries to other users in a specific geographic area and receive prompt video responses. The richness of video content provides much detail and context to the person posing the query, while consuming little attention from those who respond. The QuiltView architecture incorporates result caching, geolocation and query similarity detection to shield users from being overwhelmed by a flood of queries. More detailed information can be found in [this paper](http://www.cs.cmu.edu/~zhuoc/papers/quiltview_HotMobile2014.pdf)

## License

All source code, documentation, and related artifacts associated with the
QuiltView open source project are licensed under the [Apache License, Version
2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

A copy of this license is reproduced in the [LICENSE](LICENSE) file, and the
licenses of dependencies and included code are mentioned in the
[NOTICE](NOTICE) file.

## System Components and Workflow

The system currently consists of three components. You can find the source code for each component accordingly:

1. `server` under `/quiltview-service` - This is the part you should run it on a global server. It comes with a web interface so you can post your queries there. The server will deliver queries to clients and receive video responses back. The server stores the videos (at least for some time) to serve as a cache to shortcuit future queries.

2. `client` under `/client` - This is the Android code you should run on a Google Glass. When the Glass receives a query, it will pop up and the user can simply reply with a short video with one touch. The video will be uploaded to Youtube and the Metadata about the video, including the Youtube link, will be stored in our QuiltView server. If the user does not reply within 10 seconds, the query will disappear.

3. `proxy` under `proxy_server` - This piece exists only because we cannot find a way to store a recorded video on Glass disk with customized application. So what we do now is to stream the frames from client to the proxy, store the video there, and upload it to Youtube from proxy.

## Tested Platforms
Currently we have only tested the source code with Ubuntu 12.04 LTS 64-bit.

## Setting Up Server

0. Go to the server directory

    ```bash
    cd QUILTVIEW_ROOT
    ```

1. Install system libraries

    ```bash
    sudo apt-get update
    sudo apt-get upgrade
    sudo apt-get install python-pip mysql-server mysql-client libmysqlclient-dev python-dev python-gflags libblas-dev libatlas-dev liblapack-dev python-numpy python-scipy gfortran libevent-dev
    ```

2. Set up virtual environment
    
    ```bash
    sudo pip install virtualenv
    virtualenv --system-site-packages ENV
    source ENV/bin/activate
    ```

    Now you can go to the server directory

    ```bash
    cd quiltview_service/quiltview/
    ```

3. Install python libraries

    ```bash
    pip install -r PIP_LIB
    ```

4. Set up database (now you should have mysql installed and running)

    ```bash
    mysql -u root
    CREATE USER 'quiltview'@'localhost' IDENTIFIED BY 'quiltview2013';
    CREATE DATABASE quiltview;
    GRANT ALL ON *.* TO quiltview@localhost;
    FLUSH PRIVILEGES;
    ```

    To check if database is correctly created, do

    ```bash
    mysql -u quiltview -p
    show databases;
    ```

    Now sync database from Django to mysql

    ```bash
    ./manage.py syncdb
    ```

5. Configure Django

    *Change the hardcoded path in quiltview/settings.py to the right value (uh...)

    Create folders for static files

    ```bash
    mkdir STATIC_DIRS STATIC_ROOT
    ```

    Download boostrap, unzip it, and put it under STATIC_DIRS

    Collect static files for Django

    ```bash
    ./manage.py collectstatic
    ```

## Setting up client

    To come soon...

## Setting up proxy

    To come soon...

## Setting up virtual user

    To come soon...

Four initial users:
wenlu.c.hu@gmail.com 015DA6FC1900A01F 
zhuoc@cs.cmu.edu 0149C25E0601C019
15821.group7@gmail.com 015DA77218019009
czxxdd@gmail.com 015ECD700501801B

MAC address:
zhuo's glass f8:8f:ca:24:5f:8b
blue glass f8:8f:ca:24:8c:7d
