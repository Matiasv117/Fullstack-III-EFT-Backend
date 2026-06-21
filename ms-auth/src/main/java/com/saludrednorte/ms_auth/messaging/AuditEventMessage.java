package com.saludrednorte.ms_auth.messaging;

public class AuditEventMessage {
    private String username;
    private String action;
    private String details;
    private String origen;

    public AuditEventMessage() {}

    public AuditEventMessage(String username, String action, String details, String origen) {
        this.username = username;
        this.action = action;
        this.details = details;
        this.origen = origen;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
}
