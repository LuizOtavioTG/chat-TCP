package com.example.chat;

import com.example.chat.crypto.CaesarCipher;
import com.example.chat.crypto.Cipher;
import com.example.chat.dh.DiffieHellman;
import com.example.chat.model.KeyExchangeMessage;
import com.example.chat.model.Message;
import com.example.chat.util.ConsoleUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatClient {
    private final String host;
    private final int port;
    private final Cipher cipher;
    private final String key;
    private final boolean useDynamicCaesar;
    private final BlockingQueue<Long> keyExchangeResponses = new LinkedBlockingQueue<>();

    public ChatClient(String host, int port, Cipher cipher, String key) {
        this.host = host;
        this.port = port;
        this.cipher = cipher;
        this.key = key;
        this.useDynamicCaesar = cipher instanceof CaesarCipher;
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
                        Object payload = in.readObject();

                        if (payload instanceof KeyExchangeMessage exchangeMessage) {
                            keyExchangeResponses.offer(exchangeMessage.getPublicComponent());
                            continue;
                        }

                        if (payload instanceof Message msg) {
                            String messageKey = resolveKeyForMessage(msg);
                            String plain = cipher.decrypt(msg.getText(), messageKey);
                            System.out.println("<< " + plain);
                        }
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
                String negotiatedKey = useDynamicCaesar ? negotiateKey(out) : this.key;
                String enc = cipher.encrypt(plain, negotiatedKey);
                String type = cipher.getClass().getSimpleName();

                out.writeObject(new Message(enc, type, negotiatedKey));
                out.flush();
            }
        }
    }

    private String resolveKeyForMessage(Message msg) {
        if (useDynamicCaesar && msg.getKey() != null && !msg.getKey().isBlank()) {
            return msg.getKey();
        }
        return this.key;
    }

    private String negotiateKey(ObjectOutputStream out) throws IOException {
        DiffieHellman dh = DiffieHellman.createDefault();
        long clientPublic = dh.generatePublicComponent();

        out.writeObject(new KeyExchangeMessage(clientPublic));  //envia a key publica para o server
        out.flush();

        long serverPublic = awaitServerComponent(); // aguarda keyExchangeResponses, basicamente a transmissão de chaves
        long sharedSecret = dh.computeSharedSecret(serverPublic);
        int shift = DiffieHellman.deriveShift(sharedSecret);
        System.out.println("[DH] Nova chave negociada: " + shift);
        return Integer.toString(shift);
    }

    private long awaitServerComponent() throws IOException {
        try {
            return keyExchangeResponses.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrompido ao aguardar resposta de Diffie-Hellman.", e);
        }
    }
}
