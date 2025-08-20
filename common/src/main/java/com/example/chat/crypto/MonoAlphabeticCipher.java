package com.example.chat.crypto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MonoAlphabeticCipher implements Cipher {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** Constrói o mapa de substituição para ENCRIPTAR a partir da chave.
     *  A chave deve conter 26 letras únicas (A-Z), representando o alfabeto embaralhado. */
    private Map<Character, Character> buildEncMap(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Chave nula.");
        }
        // Aceita letras com/sem espaços, ignora caracteres não A–Z
        String cleaned = key.replaceAll("[^A-Za-z]", "").toUpperCase();

        if (cleaned.length() != 26) {
            throw new IllegalArgumentException("A chave deve ter 26 letras únicas (somente A–Z). Ex.: QWERTYUIOPASDFGHJKLZXCVBNM");
        }

        Set<Character> seen = new HashSet<>(26);
        Map<Character, Character> map = new HashMap<>(26);
        for (int i = 0; i < 26; i++) {
            char plain = ALPHABET.charAt(i);
            char subst = cleaned.charAt(i);
            if (subst < 'A' || subst > 'Z') {
                throw new IllegalArgumentException("Chave inválida: apenas letras A–Z.");
            }
            if (!seen.add(subst)) {
                throw new IllegalArgumentException("Chave inválida: letras repetidas.");
            }
            map.put(plain, subst);
        }
        return map;
    }

    /** Constrói o mapa de substituição para DECRIPTAR (inverso do encMap). */
    private Map<Character, Character> buildDecMap(Map<Character, Character> encMap) {
        Map<Character, Character> dec = new HashMap<>(26);
        for (Map.Entry<Character, Character> e : encMap.entrySet()) {
            dec.put(e.getValue(), e.getKey());
        }
        return dec;
    }

    @Override
    public String encrypt(String message, String key) {
        Map<Character, Character> enc = buildEncMap(key);
        StringBuilder sb = new StringBuilder(message.length());

        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                boolean upper = Character.isUpperCase(c);
                char base = Character.toUpperCase(c);
                Character mapped = enc.get(base);
                if (mapped != null) {
                    sb.append(upper ? mapped : Character.toLowerCase(mapped));
                } else {
                    sb.append(c); // não deveria acontecer, mas por segurança
                }
            } else {
                sb.append(c); // mantém espaços, números, pontuação
            }
        }
        return sb.toString();
    }

    @Override
    public String decrypt(String message, String key) {
        Map<Character, Character> dec = buildDecMap(buildEncMap(key));
        StringBuilder sb = new StringBuilder(message.length());

        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                boolean upper = Character.isUpperCase(c);
                char base = Character.toUpperCase(c);
                Character mapped = dec.get(base);
                if (mapped != null) {
                    sb.append(upper ? mapped : Character.toLowerCase(mapped));
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
