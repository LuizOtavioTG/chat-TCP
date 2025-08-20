package com.example.chat.crypto;

public final class CipherFactory {
    private CipherFactory() {}

    /** Retorna uma implementação de Cipher a partir do nome da cifra. */
    public static Cipher fromType(String type) {
        if (type == null) return null;
        String t = type.trim().toLowerCase();

        // nomes comuns / PT-BR
        if (t.contains("caesar") || t.contains("césar") || t.contains("cesar"))   return new CaesarCipher();
        if (t.contains("mono"))                                                   return new MonoAlphabeticCipher();
//        if (t.contains("playfair"))                                               return new PlayfairCipher();
//        if (t.contains("vigenere") || t.contains("vigenère"))                     return new VigenereCipher();

        // fallback: SimpleName das classes
        switch (type) {
            case "CaesarCipher":         return new CaesarCipher();
            case "MonoAlphabeticCipher": return new MonoAlphabeticCipher();
//            case "PlayfairCipher":       return new PlayfairCipher();
//            case "VigenereCipher":       return new VigenereCipher();
            default: return null;
        }
    }
}
