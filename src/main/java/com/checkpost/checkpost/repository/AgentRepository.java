package com.checkpost.checkpost.repository;

import com.checkpost.checkpost.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
}