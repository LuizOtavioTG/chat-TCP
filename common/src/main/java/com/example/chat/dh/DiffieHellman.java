package com.example.chat.dh;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Helper responsável por gerar componentes e chave compartilhada usando Diffie-Hellman.
 */
public class DiffieHellman {
    private static final long DEFAULT_PRIME = 23;      // número primo pequeno para fins didáticos
    private static final long DEFAULT_GENERATOR = 5;   // gerador compatível com o primo escolhido
    private static final SecureRandom RANDOM = new SecureRandom(); //gerador private keyy

    private final long prime;
    private final long generator;
    private final long privateKey;

    private DiffieHellman(long prime, long generator, long privateKey) {
        this.prime = prime;
        this.generator = generator;
        this.privateKey = privateKey;
    }

    public static DiffieHellman createDefault() {
        return new DiffieHellman(DEFAULT_PRIME, DEFAULT_GENERATOR, generateSecret());
    }

    private static long generateSecret() {
        // Mantém valores pequenos para facilitar conferência manual durante a atividade.
        return 2 + RANDOM.nextInt(10_000);
    }

    public long generatePublicComponent() {
        return modPow(generator, privateKey, prime); //g^privateKey mod p
    }

    public long computeSharedSecret(long otherPublicComponent) {
        return modPow(otherPublicComponent, privateKey, prime);
    }

    public static int deriveShift(long sharedSecret) {
        int shift = (int) (sharedSecret % 26);
        return shift <= 0 ? shift + 26 : shift;
    }

    private static long modPow(long base, long exponent, long modulus) {
        return BigInteger.valueOf(base)
                .modPow(BigInteger.valueOf(exponent), BigInteger.valueOf(modulus))
                .longValue();
    }

    public long getPrime() { return prime; }
    public long getGenerator() { return generator; }
    public long getPrivateKey() { return privateKey; }
}
