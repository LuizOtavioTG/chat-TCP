package com.example.chat.crypto;

import java.nio.charset.StandardCharsets;

public class RC4Cipher implements Cipher {

    private static byte[] rc4(byte[] data, byte[] key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("A chave do RC4 não pode ser vazia.");
        }

        // KSA
        int[] S = new int[256];
        for (int i = 0; i < 256; i++) S[i] = i;

        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + S[i] + (key[i % key.length] & 0xFF)) & 0xFF;
            int tmp = S[i]; S[i] = S[j]; S[j] = tmp;
        }

        // PRGA
        byte[] out = new byte[data.length];
        int i = 0; j = 0;
        for (int k = 0; k < data.length; k++) {
            i = (i + 1) & 0xFF;
            j = (j + S[i]) & 0xFF;
            int tmp = S[i]; S[i] = S[j]; S[j] = tmp;
            int K = S[(S[i] + S[j]) & 0xFF];
            out[k] = (byte) ((data[k] & 0xFF) ^ K);
        }
        return out;
    }

    private static String bytesToDecimalString(byte[] arr) {
        if (arr.length == 0) return "";
        StringBuilder sb = new StringBuilder(arr.length * 4);
        for (int i = 0; i < arr.length; i++) {
            int unsigned = arr[i] & 0xFF;   // 0..255
            if (i > 0) sb.append(' ');
            sb.append(unsigned);
        }
        return sb.toString();
    }

    private static byte[] decimalStringToBytes(String s) {
        s = (s == null) ? "" : s.trim();
        if (s.isEmpty()) return new byte[0];
        String[] parts = s.split("\\s+");
        byte[] out = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            int v = Integer.parseInt(parts[i]);
            if (v < 0 || v > 255) {
                throw new IllegalArgumentException("Valor decimal fora do intervalo 0..255: " + v);
            }
            out[i] = (byte) v;
        }
        return out;
    }

    @Override
    public String encrypt(String message, String key) {
        if (message == null) message = "";
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException("A chave do RC4 não pode ser vazia.");

        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        byte[] k = key.getBytes(StandardCharsets.UTF_8);
        byte[] cipherBytes = rc4(data, k);

        // Retorna em DECIMAL (ex.: "23 145 8 ...")
        return bytesToDecimalString(cipherBytes);
    }

    @Override
    public String decrypt(String message, String key) {
        if (message == null) message = "";
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException("A chave do RC4 não pode ser vazia.");

        // Espera decimal (ex.: "23 145 8 ...")
        byte[] cipherBytes = decimalStringToBytes(message);
        byte[] k = key.getBytes(StandardCharsets.UTF_8);
        byte[] plainBytes = rc4(cipherBytes, k);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }
}
