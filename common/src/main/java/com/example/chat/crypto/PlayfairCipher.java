package com.example.chat.crypto;

import java.util.HashMap;
import java.util.Map;

public class PlayfairCipher implements Cipher {

    private static final int SIZE = 5;
    private static final String ALPHABET = "ABCDEFGHIKLMNOPQRSTUVWXYZ"; // J funde com I


    private static String normalizeKey(String key) {
        if (key == null) throw new IllegalArgumentException("Chave nula.");
        // mantém só A-Z, uppercase, J->I
        StringBuilder sb = new StringBuilder();
        for (char ch : key.toUpperCase().toCharArray()) {
            if (ch >= 'A' && ch <= 'Z') {
                if (ch == 'J') ch = 'I';
                sb.append(ch);
            }
        }
        if (sb.length() == 0) throw new IllegalArgumentException("Chave vazia.");

        return sb.toString();
    }

    private static String buildKeyAlphabet(String key) {
        String norm = normalizeKey(key);
        boolean[] used = new boolean[26]; // A=0 ... Z=25 (J será tratado como I, index 8)
        StringBuilder table = new StringBuilder(25);

        // coloca letras da chave (sem duplicatas)
        for (char ch : norm.toCharArray()) {
            int idx = (ch == 'J' ? 'I' : ch) - 'A';
            if (idx == ('J' - 'A')) idx = 'I' - 'A';
            if (!used[idx] && ch != 'J') {
                used[idx] = true;
                table.append(ch);
            }
        }

        // completa com alfabeto (sem J)
        for (char ch : ALPHABET.toCharArray()) {
            int idx = ch - 'A';
            if (!used[idx]) {
                used[idx] = true;
                table.append(ch);
            }
        }


        return table.toString(); // 25 letras
    }

    private static class Table {
        final char[][] grid = new char[SIZE][SIZE];
        final Map<Character, int[]> pos = new HashMap<>(25);
    }

    private static Table buildTable(String key) {
        String keyAlpha = buildKeyAlphabet(key);
        Table t = new Table();
        int k = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                char ch = keyAlpha.charAt(k++);
                t.grid[r][c] = ch;
                t.pos.put(ch, new int[]{r, c});
            }
        }
        return t;
    }

    private static char toTableChar(char ch) {
        // uppercase + J->I
        char up = Character.toUpperCase(ch);
        return (up == 'J') ? 'I' : up;
    }

    private static char fillerForDuplicate(char ch) {
        // evita par "X X" criar loop infinito; se duplicado for X, usa Q; senão usa X
        return (ch == 'X') ? 'Q' : 'X';
    }

    // ---------- pipeline de letras ----------
    private static String lettersOnlyUpperIJ(String s) {
        StringBuilder letters = new StringBuilder(s.length());
        for (char ch : s.toCharArray()) {
            if (Character.isLetter(ch)) {
                letters.append(toTableChar(ch));
            }
        }
        return letters.toString(); // só A-Z (J->I), uppercase
    }

    private static String encryptLetters(String letters, Table t) {
        StringBuilder out = new StringBuilder(letters.length());
        int i = 0;
        while (i < letters.length()) {
            char a = letters.charAt(i);
            char b;
            if (i + 1 < letters.length()) {
                b = letters.charAt(i + 1);
                if (a == b) {
                    // insere filler entre letras iguais
                    b = fillerForDuplicate(a);
                    i += 1; // consome só a primeira; a segunda (original) será reprocessada
                } else {
                    i += 2;
                }
            } else {
                // último sozinho → preenche com X
                b = 'X';
                i += 1;
            }

            int[] pa = t.pos.get(a);
            int[] pb = t.pos.get(b);

            if (pa[0] == pb[0]) {
                // mesma linha: pega próxima coluna
                out.append(t.grid[pa[0]][(pa[1] + 1) % SIZE]);
                out.append(t.grid[pb[0]][(pb[1] + 1) % SIZE]);
            } else if (pa[1] == pb[1]) {
                // mesma coluna: pega próxima linha
                out.append(t.grid[(pa[0] + 1) % SIZE][pa[1]]);
                out.append(t.grid[(pb[0] + 1) % SIZE][pb[1]]);
            } else {
                // retângulo: troca colunas
                out.append(t.grid[pa[0]][pb[1]]);
                out.append(t.grid[pb[0]][pa[1]]);
            }
        }
        return out.toString();
    }

    private static String decryptLetters(String letters, Table t) {
        StringBuilder out = new StringBuilder(letters.length());
        int i = 0;
        while (i < letters.length()) {
            char a = letters.charAt(i);
            char b = (i + 1 < letters.length()) ? letters.charAt(i + 1) : 'X';
            i += 2;

            int[] pa = t.pos.get(a);
            int[] pb = t.pos.get(b);

            if (pa[0] == pb[0]) {
                // mesma linha: letra anterior na linha
                out.append(t.grid[pa[0]][(pa[1] - 1 + SIZE) % SIZE]);
                out.append(t.grid[pb[0]][(pb[1] - 1 + SIZE) % SIZE]);
            } else if (pa[1] == pb[1]) {
                // mesma coluna: letra anterior na coluna
                out.append(t.grid[(pa[0] - 1 + SIZE) % SIZE][pa[1]]);
                out.append(t.grid[(pb[0] - 1 + SIZE) % SIZE][pb[1]]);
            } else {
                // retângulo: troca colunas
                out.append(t.grid[pa[0]][pb[1]]);
                out.append(t.grid[pb[0]][pa[1]]);
            }
        }
        return out.toString();
    }

    private static String reinsertNonLetters(String original, String transformedLetters) {
        StringBuilder result = new StringBuilder(original.length());
        int li = 0;
        for (int i = 0; i < original.length(); i++) {
            char ch = original.charAt(i);
            if (Character.isLetter(ch)) {
                char mapped = transformedLetters.charAt(li++);
                // preserva caixa
                result.append(Character.isUpperCase(ch) ? mapped : Character.toLowerCase(mapped));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    // ---------- API ----------
    @Override
    public String encrypt(String message, String key) {
        Table t = buildTable(key);
        String letters = lettersOnlyUpperIJ(message);
        String encLetters = encryptLetters(letters, t);
        return reinsertNonLetters(message, encLetters);
    }

    @Override
    public String decrypt(String message, String key) {
        Table t = buildTable(key);
        String letters = lettersOnlyUpperIJ(message);
        String decLetters = decryptLetters(letters, t);
        return reinsertNonLetters(message, decLetters);
    }
}
