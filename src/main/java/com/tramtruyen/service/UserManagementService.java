package com.tramtruyen.service;

import com.tramtruyen.dto.*;
import org.springframework.data.domain.Page;

public interface UserManagementService {

    Page<UserManagementDTO> getUsers(String keyword, String role, String status, int page, int size);

    UserStatsDTO getUserStats();

    void assignRole(Integer adminId, AssignRoleRequest request, String ipAddress);

    void banUser(Integer adminId, BanUserRequest request, String ipAddress);

    void unbanUser(Integer adminId, UnbanUserRequest request, String ipAddress);
}
