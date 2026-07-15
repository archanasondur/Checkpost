package com.checkpost.checkpost.repository;

import com.checkpost.checkpost.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByAgentIdOrAgentIdIsNull(Long agentId);
}