package com.tramtruyen.service;

import com.tramtruyen.dto.PaymentLogDTO;
import com.tramtruyen.dto.TransactionAuditDTO;
import com.tramtruyen.dto.TransactionAuditStatsDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TransactionAuditService {

    TransactionAuditStatsDTO getAuditStats();

    Page<TransactionAuditDTO> getAuditTransactions(String startDate, String endDate, String type,
                                                   String orderCode, String email, int page, int size);

    List<PaymentLogDTO> getIpnLogsByOrderCode(String orderCode);
}
