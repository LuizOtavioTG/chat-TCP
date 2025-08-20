package com.example.chat.crypto;

public class VigenereCipher implements Cipher {

    private static String normalizeKey(String key) {
        if (key == null) throw new IllegalArgumentException("Chave nula.");

        String cleaned = key.replaceAll("[^A-Za-z]", "").toUpperCase();
        if (cleaned.isEmpty()) throw new IllegalArgumentException("A chave deve conter letras (A–Z).");
        return cleaned;
    }

    private static char encChar(char plain, char k) {
        int base = Character.isUpperCase(plain) ? 'A' : 'a';
        int p = Character.toUpperCase(plain) - 'A';
        int s = k - 'A';
        int c = (p + s) % 26;
        char up = (char) ('A' + c);
        return Character.isUpperCase(plain) ? up : Character.toLowerCase(up);
    }

    private static char decChar(char cipher, char k) {
        int base = Character.isUpperCase(cipher) ? 'A' : 'a';
        int c = Character.toUpperCase(cipher) - 'A';
        int s = k - 'A';
        int p = (c - s + 26) % 26;
        char up = (char) ('A' + p);
        return Character.isUpperCase(cipher) ? up : Character.toLowerCase(up);
    }

    @Override
    public String encrypt(String message, String key) {
        String k = normalizeKey(key);
        StringBuilder out = new StringBuilder(message.length());
        int ki = 0;

        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            if (Character.isLetter(ch)) {
                char kk = k.charAt(ki % k.length());
                out.append(encChar(ch, kk));
                ki++;
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    @Override
    public String decrypt(String message, String key) {
        String k = normalizeKey(key);
        StringBuilder out = new StringBuilder(message.length());
        int ki = 0;

        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            if (Character.isLetter(ch)) {
                char kk = k.charAt(ki % k.length());
                out.append(decChar(ch, kk));
                ki++;
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }
}
