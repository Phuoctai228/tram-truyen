package com.tramtruyen.service;

import com.tramtruyen.dto.HeaderNotificationDTO;
import org.springframework.security.core.Authentication;

public interface HeaderNotificationService {

    HeaderNotificationDTO getHeaderData(Authentication authentication);
}
