package com.example.chat;

import com.example.chat.model.Message;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public ClientHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override public void run() {
        try {
            while (true) {
                Message msg = (Message) in.readObject();
                ChatServer.broadcast(msg, out);
            }
        } catch (Exception e) {
            System.out.println("[Server] Cliente caiu: " + socket.getRemoteSocketAddress());
        }
    }
}
