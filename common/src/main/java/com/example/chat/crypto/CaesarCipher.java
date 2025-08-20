package com.example.chat.crypto;

public class CaesarCipher implements Cipher {
    private static final int ALPHABET_SIZE = 26;

    @Override
    public String encrypt(String message, String key) {
        int shift = Integer.parseInt(key) % ALPHABET_SIZE;
        StringBuilder encrypted = new StringBuilder();
        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                encrypted.append((char) ((c - base + shift) % ALPHABET_SIZE + base));
            } else {
                encrypted.append(c);
            }
        }
        return encrypted.toString();
    }

    @Override
    public String decrypt(String message, String key) {
        int shift = Integer.parseInt(key) % ALPHABET_SIZE;
        StringBuilder decrypted = new StringBuilder();
        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                decrypted.append((char) ((c - base - shift + ALPHABET_SIZE) % ALPHABET_SIZE + base));
            } else {
                decrypted.append(c);
            }
        }
        return decrypted.toString();
    }
}