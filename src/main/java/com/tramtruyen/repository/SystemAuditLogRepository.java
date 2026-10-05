package com.tramtruyen.repository;

import com.tramtruyen.entity.SystemAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemAuditLogRepository extends JpaRepository<SystemAuditLog, Integer> {

    Optional<SystemAuditLog> findFirstByTargetEntityAndTargetIdAndActionTypeOrderByCreatedAtDesc(
            String targetEntity, Integer targetId, String actionType);

    List<SystemAuditLog> findByTargetEntityAndTargetIdOrderByCreatedAtDesc(
            String targetEntity, Integer targetId);
}
