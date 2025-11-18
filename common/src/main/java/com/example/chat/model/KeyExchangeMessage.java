package com.example.chat.model;

import java.io.Serializable;

/**
 * Envelope simples usado para trocar os valores públicos do Diffie-Hellman.
 */
public class KeyExchangeMessage implements Serializable {
    private final long publicComponent;

    public KeyExchangeMessage(long publicComponent) {
        this.publicComponent = publicComponent;
    }

    public long getPublicComponent() {
        return publicComponent;
    }
}
