package com.example.fitbiteapp;

public class ChatMessage {
    private String text;
    private String role; // "user" or "assistant"

    public ChatMessage(String text, String role) {
        this.text = text;
        this.role = role;
    }

    public String getText() { return text; }
    public String getRole() { return role; }
}