package com.tramtruyen.service;

import com.tramtruyen.dto.SystemAuditLogDTO;
import com.tramtruyen.dto.SystemAuditLogDetailDTO;
import com.tramtruyen.dto.SystemAuditLogStatsDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SystemAuditLogService {

    record ActionGroupItem(String code, String name) {}

    SystemAuditLogStatsDTO getStats();

    Page<SystemAuditLogDTO> getAuditLogs(String startDate, String endDate, String actionType,
                                         String performer, String targetEntity, Integer targetId,
                                         int page, int size);

    Page<SystemAuditLogDTO> getAuditLogsFiltered(String keyword, String actionGroup, String severity,
                                                 String timeRange, String startDate, String endDate,
                                                 Boolean onlyAdmin, int page, int size);

    List<ActionGroupItem> getActiveActionGroups();

    SystemAuditLogDetailDTO getAuditLogDetail(Integer logId);

    List<String> getAvailableActionTypes();

    List<String> getAvailableTargetEntities();
}
