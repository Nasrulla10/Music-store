package com.music.musicstore.services;

import com.music.musicstore.models.AuditLog;
import com.music.musicstore.repositories.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Log an admin action
     */
    public void logAdminAction(String adminUsername, String action, String resourceType,
                              Long resourceId, String details, HttpServletRequest request) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAdminUsername(adminUsername);
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setDetails(details);
            auditLog.setSuccess(true);

            // Set request details if available
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession().getId());
            }

            // Set severity based on action type
            auditLog.setSeverity(determineSeverity(action, resourceType));

            auditLogRepository.save(auditLog);

            // Also log to application logs
            logger.info("ADMIN_ACTION: {} performed {} on {} (ID: {}) - {}",
                       adminUsername, action, resourceType, resourceId, details);

        } catch (Exception e) {
            logger.error("Failed to save audit log for admin action", e);
        }
    }

    /**
     * Log a failed admin action
     */
    public void logFailedAdminAction(String adminUsername, String action, String resourceType,
                                   Long resourceId, String errorMessage, HttpServletRequest request) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAdminUsername(adminUsername);
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setSuccess(false);
            auditLog.setErrorMessage(errorMessage);

            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession().getId());
            }

            auditLog.setSeverity(AuditLog.AuditSeverity.HIGH);

            auditLogRepository.save(auditLog);

            logger.warn("ADMIN_ACTION_FAILED: {} failed to perform {} on {} (ID: {}) - Error: {}",
                       adminUsername, action, resourceType, resourceId, errorMessage);

        } catch (Exception e) {
            logger.error("Failed to save failed audit log", e);
        }
    }

    /**
     * Get audit logs with pagination
     */
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Get audit logs by admin username
     */
    public Page<AuditLog> getAuditLogsByAdmin(String adminUsername, Pageable pageable) {
        return auditLogRepository.findByAdminUsernameOrderByTimestampDesc(adminUsername, pageable);
    }

    /**
     * Determine severity based on action type
     */
    private AuditLog.AuditSeverity determineSeverity(String action, String resourceType) {
        if (action.contains("DELETE") || action.contains("REFUND") || action.contains("SHUTDOWN")) {
            return AuditLog.AuditSeverity.CRITICAL;
        } else if (action.contains("CREATE") || action.contains("UPDATE") || action.contains("STATUS_CHANGE")) {
            return AuditLog.AuditSeverity.HIGH;
        } else if (action.contains("VIEW") || action.contains("GET") || action.contains("ANALYTICS")) {
            return AuditLog.AuditSeverity.LOW;
        } else {
            return AuditLog.AuditSeverity.MEDIUM;
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
