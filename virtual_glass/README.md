# Virtual Glass User

## What for

We'd love to test the scalability of our system without actually using hundreds of Google Glasses. So we run Python scripts on any machine to pretend to be QuiltView users. In the eyes of QuiltView service, these virtual users are indistinguishable with real users 

## What you need
- A "client\_secrets.json" for YouTube access in this directory

  It will ask for authentication only the first time. 
  Restart if it stopped after authentication.

## To Run
- python virtual\_glass.py 

  With each command, use -h for help 

- Sample usage is in start.sh and kill.sh
