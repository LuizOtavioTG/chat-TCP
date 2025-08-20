package com.example.chat.crypto;

public final class CipherFactory {
    private CipherFactory() {}


    public static Cipher fromType(String type) {
        if (type == null) return null;
        String t = type.trim().toLowerCase();


        if (t.contains("caesar") || t.contains("césar") || t.contains("cesar"))   return new CaesarCipher();
        if (t.contains("mono"))                                                   return new MonoAlphabeticCipher();
        if (t.contains("playfair"))                                               return new PlayfairCipher();
        if (t.contains("vigenere") || t.contains("vigenère"))                     return new VigenereCipher();


        switch (type) {
            case "CaesarCipher":         return new CaesarCipher();
            case "MonoAlphabeticCipher": return new MonoAlphabeticCipher();
            case "PlayfairCipher":       return new PlayfairCipher();
            case "VigenereCipher":       return new VigenereCipher();
            default: return null;
        }
    }
}
