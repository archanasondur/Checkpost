package com.checkpost.checkpost.model;

import jakarta.persistence.*;

@Entity
@Table(name = "agents")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String owner;

    @Column(name = "api_key_hash", nullable = false)
    private String apiKeyHash;

    @Column(nullable = false)
    private String status = "ACTIVE";

    public Agent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getApiKeyHash() { return apiKeyHash; }
    public void setApiKeyHash(String apiKeyHash) { this.apiKeyHash = apiKeyHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}