package com.example.chat;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.*;

public class ChatServer {
    private final int port;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    // username -> client connection
    private final ConcurrentMap<String, ClientHandler> clients = new ConcurrentHashMap<>();


    public ChatServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("Servidor ouvindo em 0.0.0.0:" + port);
            while (true) {
                Socket socket = serverSocket.accept();
                pool.submit(() -> handleNewConnection(socket));
            }
        }
    }

    private void handleNewConnection(Socket socket) {
        try {
            socket.setTcpNoDelay(true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);


            out.println("WELCOME Digite seu nome com: /join <nome>");


            String line;
            String username = null;
            while ((line = in.readLine()) != null) {
                if (username == null) {
                    if (line.startsWith("/join ")) {
                        String candidate = line.substring(6).trim();
                        if (candidate.isEmpty() || candidate.contains(" ") || candidate.length() > 20) {
                            out.println("ERROR Nome inválido. Use um identificador simples, sem espaços (até 20 chars).");
                            continue;
                        }
                        if (clients.putIfAbsent(candidate, new ClientHandler(candidate, socket, in, out)) == null) {
                            username = candidate;
                            out.println("JOINED " + username);
                            broadcast("* " + username + " entrou no chat.", username);
                            log("Cliente conectado: " + username + " " + socket.getRemoteSocketAddress());
                            break;
                        } else {
                            out.println("ERROR Nome já em uso. Tente outro.");
                        }
                    } else {
                        out.println("ERROR Primeiro comando deve ser /join <nome>");
                    }
                }
            }


            if (username == null) {
                safeClose(socket);
                return;
            }


// Loop principal de mensagens
            ClientHandler me = clients.get(username);
            try {
                String msg;
                while ((msg = me.in.readLine()) != null) {
                    if (msg.equals("/quit")) {
                        me.out.println("BYE");
                        break;
                    } else if (msg.startsWith("/w ")) {
// Whisper: /w alvo mensagem
                        String[] parts = msg.split(" ", 3);
                        if (parts.length < 3) {
                            me.out.println("ERROR Uso: /w <destino> <mensagem>");
                        } else {
                            whisper(username, parts[1], parts[2]);
                        }
                    } else if (msg.equals("/who")) {
                        me.out.println("USERS " + String.join(",", clients.keySet()));
                    } else {
                        broadcast("[" + username + "] " + msg, username);
                    }
                }
            } finally {
                disconnect(username);
            }
        } catch (IOException e) {
            log("Erro conexão: " + e.getMessage());
        }
    }

    private void whisper(String from, String to, String message) {
        ClientHandler dest = clients.get(to);
        ClientHandler src = clients.get(from);
        if (dest == null) {
            if (src != null) src.out.println("ERROR Usuário não encontrado: " + to);
            return;
        }
        dest.out.println("[privado de " + from + "] " + message);
        if (src != null) src.out.println("[privado para " + to + "] " + message);
    }

    private void broadcast(String message, String exceptUser) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (!entry.getKey().equals(exceptUser)) {
                entry.getValue().out.println(message);
            }
        }
    }

    private void disconnect(String username) {
        ClientHandler removed = clients.remove(username);
        if (removed != null) {
            broadcast("* " + username + " saiu.", username);
            safeClose(removed.socket);
            log("Cliente desconectado: " + username);
        }
    }
    private static void safeClose(Socket s) {
        try { s.close(); } catch (Exception ignored) {}
    }


    private static void log(String s) {
        System.out.println("[" + LocalTime.now() + "] " + s);
    }


    private static class ClientHandler {
        final String username;
        final Socket socket;
        final BufferedReader in;
        final PrintWriter out;
        ClientHandler(String username, Socket socket, BufferedReader in, PrintWriter out) {
            this.username = username;
            this.socket = socket;
            this.in = in;
            this.out = out;
        }
    }


    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "5000"));
        new ChatServer(port).start();
    }
}