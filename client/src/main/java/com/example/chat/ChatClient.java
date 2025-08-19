package com.example.chat;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ChatClient {
    public static void main(String[] args) throws Exception {
        String host = System.getenv().getOrDefault("HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "5000"));
        String username = System.getenv().getOrDefault("USERNAME", "");


        try (Socket socket = new Socket(host, port)) {
            System.out.println("Conectado a " + host + ":" + port);


            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);


// Reader thread
            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException ignored) { }
            });
            reader.setDaemon(true);
            reader.start();


            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));


            if (username == null || username.isBlank()) {
                System.out.print("Digite seu nome: ");
                username = Objects.requireNonNullElse(console.readLine(), "user").trim();
            }


            out.println("/join " + username);


            String input;
            while ((input = console.readLine()) != null) {
                out.println(input);
                if ("/quit".equals(input)) {
                    break;
                }
            }
        }
    }
}