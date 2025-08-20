package com.example.chat.model;

import java.io.Serializable;

public class Message implements Serializable {
    private final String text;
    private final String cipherType;
    private final String key;

    public Message(String text, String cipherType, String key) {
        this.text = text;
        this.cipherType = cipherType;
        this.key = key;
    }

    public String getText() { return text; }
    public String getCipherType() { return cipherType; }
    public String getKey() { return key; }
}
