package com.example.chat.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DESCipher implements Cipher {

    // --- Tabelas do DES (IP, FP, E, P, S-Boxes, PC-1, PC-2, shifts) ---
    private static final int[] IP = {
            58,50,42,34,26,18,10,2, 60,52,44,36,28,20,12,4, 62,54,46,38,30,22,14,6,
            64,56,48,40,32,24,16,8, 57,49,41,33,25,17,9,1, 59,51,43,35,27,19,11,3,
            61,53,45,37,29,21,13,5, 63,55,47,39,31,23,15,7
    };
    private static final int[] FP = {
            40,8,48,16,56,24,64,32, 39,7,47,15,55,23,63,31, 38,6,46,14,54,22,62,30,
            37,5,45,13,53,21,61,29, 36,4,44,12,52,20,60,28, 35,3,43,11,51,19,59,27,
            34,2,42,10,50,18,58,26, 33,1,41,9,49,17,57,25
    };
    private static final int[] E = {
            32,1,2,3,4,5, 4,5,6,7,8,9, 8,9,10,11,12,13,
            12,13,14,15,16,17, 16,17,18,19,20,21,
            20,21,22,23,24,25, 24,25,26,27,28,29, 28,29,30,31,32,1
    };
    private static final int[] P = {
            16,7,20,21, 29,12,28,17, 1,15,23,26, 5,18,31,10,
            2,8,24,14, 32,27,3,9, 19,13,30,6, 22,11,4,25
    };
    private static final int[][][] S = {
            { // S1
                    {14,4,13,1,2,15,11,8,3,10,6,12,5,9,0,7},
                    {0,15,7,4,14,2,13,1,10,6,12,11,9,5,3,8},
                    {4,1,14,8,13,6,2,11,15,12,9,7,3,10,5,0},
                    {15,12,8,2,4,9,1,7,5,11,3,14,10,0,6,13}
            },
            { // S2
                    {15,1,8,14,6,11,3,4,9,7,2,13,12,0,5,10},
                    {3,13,4,7,15,2,8,14,12,0,1,10,6,9,11,5},
                    {0,14,7,11,10,4,13,1,5,8,12,6,9,3,2,15},
                    {13,8,10,1,3,15,4,2,11,6,7,12,0,5,14,9}
            },
            { // S3
                    {10,0,9,14,6,3,15,5,1,13,12,7,11,4,2,8},
                    {13,7,0,9,3,4,6,10,2,8,5,14,12,11,15,1},
                    {13,6,4,9,8,15,3,0,11,1,2,12,5,10,14,7},
                    {1,10,13,0,6,9,8,7,4,15,14,3,11,5,2,12}
            },
            { // S4
                    {7,13,14,3,0,6,9,10,1,2,8,5,11,12,4,15},
                    {13,8,11,5,6,15,0,3,4,7,2,12,1,10,14,9},
                    {10,6,9,0,12,11,7,13,15,1,3,14,5,2,8,4},
                    {3,15,0,6,10,1,13,8,9,4,5,11,12,7,2,14}
            },
            { // S5
                    {2,12,4,1,7,10,11,6,8,5,3,15,13,0,14,9},
                    {14,11,2,12,4,7,13,1,5,0,15,10,3,9,8,6},
                    {4,2,1,11,10,13,7,8,15,9,12,5,6,3,0,14},
                    {11,8,12,7,1,14,2,13,6,15,0,9,10,4,5,3}
            },
            { // S6
                    {12,1,10,15,9,2,6,8,0,13,3,4,14,7,5,11},
                    {10,15,4,2,7,12,9,5,6,1,13,14,0,11,3,8},
                    {9,14,15,5,2,8,12,3,7,0,4,10,1,13,11,6},
                    {4,3,2,12,9,5,15,10,11,14,1,7,6,0,8,13}
            },
            { // S7
                    {4,11,2,14,15,0,8,13,3,12,9,7,5,10,6,1},
                    {13,0,11,7,4,9,1,10,14,3,5,12,2,15,8,6},
                    {1,4,11,13,12,3,7,14,10,15,6,8,0,5,9,2},
                    {6,11,13,8,1,4,10,7,9,5,0,15,14,2,3,12}
            },
            { // S8
                    {13,2,8,4,6,15,11,1,10,9,3,14,5,0,12,7},
                    {1,15,13,8,10,3,7,4,12,5,6,11,0,14,9,2},
                    {7,11,4,1,9,12,14,2,0,6,10,13,15,3,5,8},
                    {2,1,14,7,4,10,8,13,15,12,9,0,3,5,6,11}
            }
    };
    private static final int[] PC1 = {
            57,49,41,33,25,17,9, 1,58,50,42,34,26,18, 10,2,59,51,43,35,27,
            19,11,3,60,52,44,36, 63,55,47,39,31,23,15, 7,62,54,46,38,30,22,
            14,6,61,53,45,37,29, 21,13,5,28,20,12,4
    };
    private static final int[] PC2 = {
            14,17,11,24,1,5, 3,28,15,6,21,10, 23,19,12,4,26,8,
            16,7,27,20,13,2, 41,52,31,37,47,55, 30,40,51,45,33,48,
            44,49,39,56,34,53, 46,42,50,36,29,32
    };
    private static final int[] SHIFTS = {1,1,2,2,2,2,2,2, 1,2,2,2,2,2,2,1};

    // ----- API -----
    @Override
    public String encrypt(String message, String key) {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        data = pkcs7Pad(data, 8);
        byte[] k = make8BytesKey(key);

        long[] subkeys = subkeys(k);
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i += 8) {
            long block = bytesToLong(data, i);
            long c = desBlock(block, subkeys, true);
            longToBytes(c, out, i);
        }
        return toHex(out); // HEX para o servidor comparar no simulador
    }

    @Override
    public String decrypt(String message, String key) {
        byte[] data = fromHex(message); // recebe HEX
        byte[] k = make8BytesKey(key);

        long[] subkeys = subkeys(k);
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i += 8) {
            long block = bytesToLong(data, i);
            long p = desBlock(block, subkeys, false);
            longToBytes(p, out, i);
        }
        out = pkcs7Unpad(out, 8);
        return new String(out, StandardCharsets.UTF_8);
    }

    // ----- DES core -----
    private static long desBlock(long block, long[] subkeys, boolean enc) {
        long ip = permute64(block, IP);
        int L = (int) (ip >>> 32);
        int R = (int) (ip & 0xFFFFFFFFL);

        if (enc) {
            for (int r = 0; r < 16; r++) {
                int tmp = R;
                R = L ^ f(R, subkeys[r]);
                L = tmp;
            }
        } else {
            for (int r = 15; r >= 0; r--) {
                int tmp = R;
                R = L ^ f(R, subkeys[r]);
                L = tmp;
            }
        }
        long preOut = (((long) R) << 32) | (L & 0xFFFFFFFFL); // swap
        return permute64(preOut, FP);
    }

    private static int f(int R, long subkey48) {
        long ER = permute32to48(R, E);
        long x = ER ^ subkey48; // 48 bits
        int sOut = sBoxes(x);
        return permute32(sOut, P);
    }

    private static int sBoxes(long x48) {
        int out32 = 0;
        for (int i = 0; i < 8; i++) {
            int six = (int) ((x48 >>> ((7 - i) * 6)) & 0x3F);
            int row = ((six & 0x20) >>> 4) | (six & 0x01); // b1b6
            int col = (six >>> 1) & 0x0F;                  // b2..b5
            int s = S[i][row][col] & 0x0F;
            out32 = (out32 << 4) | s;
        }
        return out32;
    }

    private static long[] subkeys(byte[] key8) {
        long k64 = bytesToLong(key8, 0);
        long k56 = permute64to56(k64, PC1);
        int C = (int) ((k56 >>> 28) & 0x0FFFFFFF);
        int D = (int) (k56 & 0x0FFFFFFF);

        long[] ks = new long[16];
        for (int r = 0; r < 16; r++) {
            C = ((C << SHIFTS[r]) | (C >>> (28 - SHIFTS[r]))) & 0x0FFFFFFF;
            D = ((D << SHIFTS[r]) | (D >>> (28 - SHIFTS[r]))) & 0x0FFFFFFF;
            long cd = (((long) C) << 28) | (D & 0x0FFFFFFFL);
            ks[r] = permute56to48(cd, PC2);
        }
        return ks;
    }

    // ----- Permutações utilitárias -----
    private static long permute64(long in, int[] table) {
        long out = 0L;
        for (int t : table) {
            out <<= 1;
            int bit = (int) ((in >>> (64 - t)) & 1L);
            out |= bit;
        }
        return out;
    }
    private static long permute64to56(long in, int[] table) { // PC-1
        long out = 0L;
        for (int t : table) {
            out <<= 1;
            int bit = (int) ((in >>> (64 - t)) & 1L);
            out |= bit;
        }
        return out & 0x00FFFFFFFFFFFFFFL; // 56 bits
    }
    private static long permute56to48(long in, int[] table) { // PC-2
        long out = 0L;
        for (int t : table) {
            out <<= 1;
            int bit = (int) ((in >>> (56 - t)) & 1L);
            out |= bit;
        }
        return out & 0x0000FFFFFFFFFFFFL; // 48 bits
    }
    private static long permute32to48(int in, int[] table) {
        long out = 0L;
        for (int t : table) {
            out <<= 1;
            int bit = (in >>> (32 - t)) & 1;
            out |= bit;
        }
        return out & 0x0000FFFFFFFFFFFFL;
    }
    private static int permute32(int in, int[] table) {
        int out = 0;
        for (int t : table) {
            out <<= 1;
            int bit = (in >>> (32 - t)) & 1;
            out |= bit;
        }
        return out;
    }

    // ----- Bytes/long + padding + hex -----
    private static long bytesToLong(byte[] b, int off) {
        return ((b[off] & 0xFFL) << 56) | ((b[off+1] & 0xFFL) << 48) |
                ((b[off+2] & 0xFFL) << 40) | ((b[off+3] & 0xFFL) << 32) |
                ((b[off+4] & 0xFFL) << 24) | ((b[off+5] & 0xFFL) << 16) |
                ((b[off+6] & 0xFFL) << 8)  | ((b[off+7] & 0xFFL));
    }
    private static void longToBytes(long v, byte[] out, int off) {
        out[off]   = (byte)(v >>> 56); out[off+1] = (byte)(v >>> 48);
        out[off+2] = (byte)(v >>> 40); out[off+3] = (byte)(v >>> 32);
        out[off+4] = (byte)(v >>> 24); out[off+5] = (byte)(v >>> 16);
        out[off+6] = (byte)(v >>> 8);  out[off+7] = (byte)(v);
    }
    private static byte[] pkcs7Pad(byte[] data, int block) {
        int pad = block - (data.length % block);
        if (pad == 0) pad = block;
        byte[] out = Arrays.copyOf(data, data.length + pad);
        Arrays.fill(out, data.length, out.length, (byte) pad);
        return out;
    }
    private static byte[] pkcs7Unpad(byte[] data, int block) {
        if (data.length == 0 || data.length % block != 0) return data;
        int pad = data[data.length - 1] & 0xFF;
        if (pad <= 0 || pad > block) return data;
        for (int i = data.length - pad; i < data.length; i++) {
            if ((data[i] & 0xFF) != pad) return data;
        }
        return Arrays.copyOf(data, data.length - pad);
    }
    private static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) {
            sb.append(String.format("%02X", x));
        }
        return sb.toString();
    }
    private static byte[] fromHex(String s) {
        String hex = s.replaceAll("\\s+", "");
        if (hex.length() % 2 != 0) throw new IllegalArgumentException("HEX ímpar.");
        byte[] out = new byte[hex.length()/2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return out;
    }

    // Chave: aceita 16 hex (64 bits) ou string ASCII (usa 8 primeiros bytes)
    private static byte[] make8BytesKey(String key) {
        if (key == null || key.isEmpty()) throw new IllegalArgumentException("Chave DES vazia.");
        String k = key.trim();
        if (k.matches("(?i)^[0-9a-f]{16}$")) {
            return fromHex(k);
        }
        byte[] raw = k.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[8];
        for (int i = 0; i < 8; i++) out[i] = (i < raw.length ? raw[i] : 0);
        return out;
    }
}
