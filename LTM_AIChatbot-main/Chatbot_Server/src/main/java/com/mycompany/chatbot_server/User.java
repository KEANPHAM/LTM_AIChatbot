package com.mycompany.chatbot_server;
public class User {
    private String username;
    private String passwordHash;
    private String currentSummary;

    public User(String username, String passwordHash, String currentSummary) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.currentSummary = currentSummary;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getCurrentSummary() { return currentSummary; }
}