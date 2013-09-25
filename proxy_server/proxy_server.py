# TODO: description

import socket


def startServer(host, port, buf):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind((HOST, PORT))
    s.listen(0)
    while True:
        conn, addr = s.accept()
        print 'Connected by', addr
        while True:
            data = conn.recv(buf)
            if not data: 
                print "Connection terminated by the other side"
                break
            print "Got something"
        conn.close()

if __name__ == "__main__":
    HOST = ""
    PORT = 7950
    BUF = 1024
    startServer(HOST, PORT, BUF)
