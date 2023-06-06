# Python server for authentication system

import socket
import sys
import os

# import database from db.json file
with open('db.json', 'r') as f:
    db = eval(f.read())

# create socket
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# bind socket to port on localhost port 21234
s.bind(("localhost", 21234))

# listen for connections
s.listen(5)


def check_user(msg):
    # the message structure is as follows:
    # <username>;<password>;<action>;
    for user in db:
        if user["name"] == msg.split(";")[0]:
            if user["password"] == msg.split(";")[1]:
                return True
            else:
                return False
    return True


def is_login_action(msg):
    # the message structure is as follows:
    # <username>;<password>;<action>;
    return msg.split(";")[2] == "login"


def update_db(msg):
    # the message structure is as follows:
    # <username>;<password>;<action>;
    # start by checking if user exists
    for user in db:
        if user["name"] == msg.split(";")[0]:
            return False

    id = 1
    for user in db:
        id += 1

    # add user to database
    db.append({"id": id, "name": msg.split(";")[
              0], "password": msg.split(";")[1]})

    with open('db.json', 'w') as f:
        f.write(str(db))

    return True


# accept connections
while True:
    # accept connection
    print("Waiting for connections...")
    clientsocket, address = s.accept()
    print(f"Connection from {address} has been established!")

    # receive data from client
    msg = clientsocket.recv(1024).decode("utf-8")
    print(msg)

    # check action
    if is_login_action(msg):
        # check if user exists
        if check_user(msg):
            # send success message
            clientsocket.send(bytes("success", "utf-8"))
        else:
            # send failure message
            clientsocket.send(bytes("failure", "utf-8"))
    else:
        # create user
        if update_db(msg):
            # send success message
            clientsocket.send(bytes("success", "utf-8"))
        else:
            # send failure message
            clientsocket.send(bytes("failure", "utf-8"))

    # close connection
    clientsocket.close()
