package com.example.chat;

import com.example.chat.crypto.Cipher;
import com.example.chat.model.Message;
import com.example.chat.util.ConsoleUtils;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private final String host;
    private final int port;
    private final Cipher cipher;
    private final String key;

    public ChatClient(String host, int port, Cipher cipher, String key) {
        this.host = host;
        this.port = port;
        this.cipher = cipher;
        this.key = key;
    }

    public void start() throws IOException {
        try (Socket socket = new Socket(host, port)) {
            System.out.println("Conectado a " + host + ":" + port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Thread de leitura (descriptografa automaticamente)
            Thread reader = new Thread(() -> {
                try {
                    while (true) {
                        Message msg = (Message) in.readObject();
                        String plain = cipher.decrypt(msg.getText(), msg.getKey());
                        System.out.println("<< " + plain);
                    }
                } catch (Exception e) {
                    System.out.println("Conexão encerrada.");
                }
            });
            reader.setDaemon(true);
            reader.start();

            // Envio
            while (true) {
                String plain = ConsoleUtils.readLine(">> ");
                String enc = cipher.encrypt(plain, key);
                out.writeObject(new Message(enc, cipher.getClass().getSimpleName(), key));
                out.flush();
            }
        }
    }
}
