package com.example.chat;

import com.example.chat.dh.DiffieHellman;
import com.example.chat.model.KeyExchangeMessage;
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
                Object payload = in.readObject();

                if (payload instanceof KeyExchangeMessage exchangeMessage) {
                    handleKeyExchange(exchangeMessage);
                    continue;
                }

                if (payload instanceof Message msg) {
                    ChatServer.broadcast(msg, out);
                    continue;
                }

                System.out.println("[Server] Tipo de mensagem desconhecido recebido: " + payload);
            }
        } catch (Exception e) {
            System.out.println("[Server] Cliente caiu: " + socket.getRemoteSocketAddress());
        }
    }

    private void handleKeyExchange(KeyExchangeMessage clientComponent) throws IOException {
        DiffieHellman dh = DiffieHellman.createDefault();
        long serverPublic = dh.generatePublicComponent();

        long sharedSecret = dh.computeSharedSecret(clientComponent.getPublicComponent());
        DiffieHellman.deriveShift(sharedSecret); // garante cálculo da chave, embora não seja usada aqui

        // Envia o componente público negociado apenas para o cliente solicitante
        out.writeObject(new KeyExchangeMessage(serverPublic));
        out.flush();
    }
}
