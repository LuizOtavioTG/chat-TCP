package com.example.chat;

import com.example.chat.crypto.*;
import com.example.chat.util.ConsoleUtils;

public class ChatApp {
    public static void main(String[] args) throws Exception {
        String defaultHost = System.getenv().getOrDefault("HOST", "server");
        String defaultPort = System.getenv().getOrDefault("PORT", "5000");

        String host = ConsoleUtils.readLine("Digite o IP/host do servidor (ENTER = " + defaultHost + "): ");
        if (host == null || host.isBlank()) host = defaultHost;

        String portStr = ConsoleUtils.readLine("Digite a PORTA (ENTER = " + defaultPort + "): ");
        int port = (portStr == null || portStr.isBlank()) ? Integer.parseInt(defaultPort)
                : Integer.parseInt(portStr.trim());

        System.out.println("Escolha a cifra:");
        System.out.println("1 - César");
        System.out.println("2 - Substituição Monoalfabética");
        System.out.println("3 - Playfair");
        System.out.println("4 - Vigenère");
        System.out.println("5 - RC4");
        System.out.println("6 - DES");
        System.out.println("7 - AES");
        int choice = ConsoleUtils.readInt("Opção: ");

        Cipher cipher = switch (choice) {
            case 1 -> new CaesarCipher();
            case 2 -> new MonoAlphabeticCipher();
            case 3 -> new PlayfairCipher();
            case 4 -> new VigenereCipher();
            case 5 -> new RC4Cipher();
            case 6 -> new DESCipher();
            case 7 -> new AESCipher();
            default -> throw new IllegalArgumentException("Opção inválida");
        };

        String keyPrompt = (choice == 1)
                ? "Digite a chave (César = número inteiro): "
                : "Digite a chave: ";
        String key = ConsoleUtils.readLine(keyPrompt).trim();

        // Validação simples para César
        if (choice == 1) {
            try { Integer.parseInt(key); }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Para a Cifra de César, a chave deve ser um número inteiro.");
            }
        }

        ChatClient client = new ChatClient(host, port, cipher, key);
        client.start();
    }
}
