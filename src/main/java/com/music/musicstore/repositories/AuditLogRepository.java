package com.music.musicstore.repositories;

import com.music.musicstore.models.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find audit logs by admin username
    Page<AuditLog> findByAdminUsernameOrderByTimestampDesc(String adminUsername, Pageable pageable);

    // Find audit logs by action type
    Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    // Find audit logs by resource type
    Page<AuditLog> findByResourceTypeOrderByTimestampDesc(String resourceType, Pageable pageable);

    // Find audit logs by severity
    Page<AuditLog> findBySeverityOrderByTimestampDesc(AuditLog.AuditSeverity severity, Pageable pageable);

    // Find audit logs within date range
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find failed operations
    Page<AuditLog> findBySuccessFalseOrderByTimestampDesc(Pageable pageable);

    // Find audit logs for specific resource
    List<AuditLog> findByResourceTypeAndResourceIdOrderByTimestampDesc(String resourceType, Long resourceId);

    // Count logs by admin and date range
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.adminUsername = :username AND a.timestamp BETWEEN :startDate AND :endDate")
    Long countByAdminAndDateRange(@Param("username") String username, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Get recent critical actions
    @Query("SELECT a FROM AuditLog a WHERE a.severity = 'CRITICAL' ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentCriticalActions(Pageable pageable);

    // Security monitoring - find multiple failed actions by same admin
    @Query("SELECT a FROM AuditLog a WHERE a.adminUsername = :username AND a.success = false AND a.timestamp > :since ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedActionsByAdminSince(@Param("username") String username, @Param("since") LocalDateTime since);

    // Get audit summary by action type
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate GROUP BY a.action")
    List<Object[]> getActionSummary(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
