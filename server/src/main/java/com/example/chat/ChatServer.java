package com.example.chat;

import com.example.chat.model.Message;
import java.io.*;
import java.net.*;
import java.util.*;
import com.example.chat.crypto.Cipher;
import com.example.chat.crypto.CipherFactory;

public class ChatServer {
    private static final List<ObjectOutputStream> clients =
            Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "5000"));
        System.out.println("[Server] Iniciando na porta " + port + " ...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server] Ouvindo em 0.0.0.0:" + port);
            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("[Server] Novo cliente: " + s.getRemoteSocketAddress());
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream  in  = new ObjectInputStream(s.getInputStream());
                clients.add(out);
                new Thread(new ClientHandler(s, in, out)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(Message msg, ObjectOutputStream sender) {
        System.out.println("[Servidor recebeu - criptografada]: " + msg.getText());
        synchronized (clients) {
            Iterator<ObjectOutputStream> it = clients.iterator();
            while (it.hasNext()) {
                ObjectOutputStream out = it.next();
                if (out == sender) continue;
                try {
                    out.writeObject(msg);
                    out.flush();
                } catch (IOException e) {
                    it.remove(); // remove cliente morto
                }
            }
        }
    }
}
