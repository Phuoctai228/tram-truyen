package com.tramtruyen.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tramtruyen.dto.PaymentLogDTO;
import com.tramtruyen.dto.TransactionAuditDTO;
import com.tramtruyen.dto.TransactionAuditStatsDTO;
import com.tramtruyen.entity.PaymentLog;
import com.tramtruyen.repository.PaymentLogRepository;
import com.tramtruyen.service.TransactionAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.tramtruyen.util.DateTimeUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAuditServiceImpl implements TransactionAuditService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PaymentLogRepository paymentLogRepository;
    private final ObjectMapper objectMapper;

    private static final DecimalFormat VND_FORMAT;
    private static final DecimalFormat COIN_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
        symbols.setGroupingSeparator('.');
        VND_FORMAT = new DecimalFormat("#,###", symbols);
        COIN_FORMAT = new DecimalFormat("#,###", symbols);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionAuditStatsDTO getAuditStats() {
        String sqlTopupVnd = "SELECT COALESCE(SUM(amount_vnd), 0) FROM topup_orders WHERE status = 'SUCCESS'";
        String sqlCoinIssued = "SELECT COALESCE(SUM(coin_received), 0) FROM topup_orders WHERE status = 'SUCCESS'";
        String sqlCoinSpent = "SELECT COALESCE(SUM(ABS(amount)), 0) FROM transactions WHERE amount < 0";
        String sqlTotalTx = "SELECT (SELECT COUNT(*) FROM topup_orders) + (SELECT COUNT(*) FROM transactions WHERE type != 'DEPOSIT')";
        String sqlSuccessOrders = "SELECT COUNT(*) FROM topup_orders WHERE status = 'SUCCESS'";

        Long totalTopupVnd = jdbcTemplate.getJdbcTemplate().queryForObject(sqlTopupVnd, Long.class);
        Long totalCoinIssued = jdbcTemplate.getJdbcTemplate().queryForObject(sqlCoinIssued, Long.class);
        Long totalCoinSpent = jdbcTemplate.getJdbcTemplate().queryForObject(sqlCoinSpent, Long.class);
        Long totalTxCount = jdbcTemplate.getJdbcTemplate().queryForObject(sqlTotalTx, Long.class);
        Long successOrdersCount = jdbcTemplate.getJdbcTemplate().queryForObject(sqlSuccessOrders, Long.class);

        totalTopupVnd = totalTopupVnd != null ? totalTopupVnd : 0L;
        totalCoinIssued = totalCoinIssued != null ? totalCoinIssued : 0L;
        totalCoinSpent = totalCoinSpent != null ? totalCoinSpent : 0L;
        totalTxCount = totalTxCount != null ? totalTxCount : 0L;
        successOrdersCount = successOrdersCount != null ? successOrdersCount : 0L;

        double ratio = 0.0;
        if (totalCoinIssued > 0) {
            ratio = ((double) totalCoinSpent / totalCoinIssued) * 100.0;
            if (ratio > 100.0) ratio = 100.0;
        }

        return TransactionAuditStatsDTO.builder()
                .totalTopupVnd(totalTopupVnd)
                .totalCoinIssued(totalCoinIssued)
                .totalCoinSpent(totalCoinSpent)
                .totalTransactionsCount(totalTxCount)
                .successTopupOrdersCount(successOrdersCount)
                .topupGrowthText("—")
                .spendRatioText(String.format(Locale.US, "%.1f%%", ratio))
                .spendRatioPercent(ratio)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionAuditDTO> getAuditTransactions(String startDate, String endDate, String type,
                                                          String orderCode, String email, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        MapSqlParameterSource params = new MapSqlParameterSource();

        // 1. Lọc khoảng ngày
        LocalDate startLocalDate = parseDate(startDate);
        LocalDate endLocalDate = parseDate(endDate);

        if (startLocalDate != null && endLocalDate != null && !startLocalDate.isAfter(endLocalDate)) {
            whereClause.append(" AND combined.tx_time >= :startDate AND combined.tx_time <= :endDate ");
            LocalDateTime startUtc = DateTimeUtils.hanoiToUtc(startLocalDate.atStartOfDay());
            LocalDateTime endUtc = DateTimeUtils.hanoiToUtc(endLocalDate.atTime(LocalTime.MAX));
            params.addValue("startDate", Timestamp.valueOf(startUtc));
            params.addValue("endDate", Timestamp.valueOf(endUtc));
        } else if (startLocalDate != null && endLocalDate == null) {
            whereClause.append(" AND combined.tx_time >= :startDate ");
            LocalDateTime startUtc = DateTimeUtils.hanoiToUtc(startLocalDate.atStartOfDay());
            params.addValue("startDate", Timestamp.valueOf(startUtc));
        } else if (startLocalDate == null && endLocalDate != null) {
            whereClause.append(" AND combined.tx_time <= :endDate ");
            LocalDateTime endUtc = DateTimeUtils.hanoiToUtc(endLocalDate.atTime(LocalTime.MAX));
            params.addValue("endDate", Timestamp.valueOf(endUtc));
        }

        // 2. Lọc loại giao dịch
        if (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type.trim())) {
            String normType = type.trim().toUpperCase();
            if ("TOPUP".equals(normType) || "NAP_TIEN".equals(normType)) {
                whereClause.append(" AND combined.source_type = 'TOPUP' ");
            } else if ("UNLOCK_CHAPTER".equals(normType) || "MO_KHOA".equals(normType)) {
                whereClause.append(" AND combined.source_type = 'LEDGER' AND combined.tx_type = 'UNLOCK_CHAPTER' ");
            } else if ("OTHER".equals(normType) || "KHAC".equals(normType) || "GIFT_COIN".equals(normType)) {
                if ("GIFT_COIN".equals(normType)) {
                    whereClause.append(" AND combined.source_type = 'LEDGER' AND combined.tx_type = 'GIFT_COIN' ");
                } else {
                    whereClause.append(" AND combined.source_type = 'LEDGER' AND combined.tx_type <> 'UNLOCK_CHAPTER' ");
                }
            }
        }

        // 3. Lọc mã đơn (chỉ khớp dòng nạp tiền, không phân biệt hoa thường)
        if (orderCode != null && !orderCode.trim().isEmpty()) {
            whereClause.append(" AND (combined.order_code IS NOT NULL AND LOWER(combined.order_code) LIKE :orderCodePattern) ");
            params.addValue("orderCodePattern", "%" + orderCode.trim().toLowerCase() + "%");
        }

        // 4. Lọc email người dùng (không phân biệt hoa thường)
        if (email != null && !email.trim().isEmpty()) {
            whereClause.append(" AND (LOWER(combined.user_email) LIKE :emailPattern OR LOWER(combined.user_name) LIKE :emailPattern) ");
            params.addValue("emailPattern", "%" + email.trim().toLowerCase() + "%");
        }

        String baseCte = """
            WITH combined AS (
                SELECT 
                    'TOPUP' AS source_type,
                    o.id AS record_id,
                    o.order_code AS order_code,
                    o.user_id AS user_id,
                    u.full_name AS user_name,
                    u.email AS user_email,
                    u.avatar_url AS user_avatar,
                    'TOPUP' AS tx_type,
                    COALESCE(cp.name, 'Nạp Coin qua VNPay') AS detail,
                    o.coin_received AS coin_amount,
                    o.amount_vnd AS amount_vnd,
                    o.bank_transaction_code AS bank_code,
                    o.status AS status,
                    COALESCE(o.paid_at, o.created_at) AS tx_time
                FROM topup_orders o
                JOIN users u ON o.user_id = u.id
                LEFT JOIN coin_packages cp ON o.package_id = cp.id

                UNION ALL

                SELECT 
                    'LEDGER' AS source_type,
                    t.id AS record_id,
                    NULL AS order_code,
                    t.user_id AS user_id,
                    u.full_name AS user_name,
                    u.email AS user_email,
                    u.avatar_url AS user_avatar,
                    t.type AS tx_type,
                    t.description AS detail,
                    t.amount AS coin_amount,
                    NULL AS amount_vnd,
                    NULL AS bank_code,
                    'RECORDED' AS status,
                    t.created_at AS tx_time
                FROM transactions t
                JOIN users u ON t.user_id = u.id
                WHERE t.type != 'DEPOSIT'
            )
            """;

        String countSql = baseCte + " SELECT COUNT(*) FROM combined " + whereClause;
        Long totalElements = jdbcTemplate.queryForObject(countSql, params, Long.class);
        totalElements = totalElements != null ? totalElements : 0L;

        String querySql = baseCte +
                " SELECT * FROM combined " +
                whereClause +
                " ORDER BY combined.tx_time DESC LIMIT :limit OFFSET :offset ";

        params.addValue("limit", size);
        params.addValue("offset", page * size);

        List<TransactionAuditDTO> list = jdbcTemplate.query(querySql, params, (rs, rowNum) -> {
            String sourceType = rs.getString("source_type");
            Integer recordId = rs.getInt("record_id");
            String oc = rs.getString("order_code");
            Integer userId = rs.getInt("user_id");
            String userName = rs.getString("user_name");
            String userEmail = rs.getString("user_email");
            String userAvatar = rs.getString("user_avatar");
            String txType = rs.getString("tx_type");
            String detail = rs.getString("detail");
            int coinAmount = rs.getInt("coin_amount");
            int amountVnd = rs.getInt("amount_vnd");
            String bankCode = rs.getString("bank_code");
            String status = rs.getString("status");
            Timestamp txTimestamp = rs.getTimestamp("tx_time");
            LocalDateTime txTime = txTimestamp != null ? txTimestamp.toLocalDateTime() : LocalDateTime.now();

            String displayCode;
            String typeDisplay;
            String typeBadgeClass;
            String coinAmountDisplay;
            String coinAmountClass;
            String amountVndDisplay;
            String statusDisplay;
            String statusBadgeClass;
            boolean hasIpnLog = false;

            if ("TOPUP".equals(sourceType)) {
                hasIpnLog = true;
                displayCode = oc != null ? oc : ("#TOPUP-" + recordId);
                typeDisplay = "Nạp tiền";
                typeBadgeClass = "bg-[#fef4e8] text-[#c07c2a] border border-[#f5d9b8]";
                coinAmountDisplay = "+" + COIN_FORMAT.format(coinAmount) + " Coin";
                coinAmountClass = "text-emerald-600 font-bold";
                amountVndDisplay = "+" + VND_FORMAT.format(amountVnd) + " đ";

                if ("SUCCESS".equalsIgnoreCase(status)) {
                    statusDisplay = "Thành công";
                    statusBadgeClass = "bg-[#e8f7ee] text-[#1b7a42] border border-[#b8e8cb]";
                } else if ("PENDING".equalsIgnoreCase(status)) {
                    statusDisplay = "Chờ thanh toán";
                    statusBadgeClass = "bg-[#fef8e7] text-[#9c6a15] border border-[#f5e3b5]";
                } else {
                    statusDisplay = "Thất bại";
                    statusBadgeClass = "bg-[#fdf0ee] text-[#a53b27] border border-[#f7c8c0]";
                }
            } else {
                displayCode = "#TX-" + (recordId != null ? (98000 + recordId) : recordId);
                if ("UNLOCK_CHAPTER".equalsIgnoreCase(txType)) {
                    typeDisplay = "Mở khóa chương";
                    typeBadgeClass = "bg-[#fdf0ee] text-[#a53b27] border border-[#f7c8c0]";
                } else if ("GIFT_COIN".equalsIgnoreCase(txType)) {
                    typeDisplay = "Thưởng điểm danh";
                    typeBadgeClass = "bg-[#fef4e8] text-[#c07c2a] border border-[#f5d9b8]";
                } else {
                    typeDisplay = txType != null ? txType : "Khác";
                    typeBadgeClass = "bg-[#f3f0ec] text-[#6d5b4a] border border-[#ddd3c8]";
                }

                if (coinAmount > 0) {
                    coinAmountDisplay = "+" + COIN_FORMAT.format(coinAmount) + " Coin";
                    coinAmountClass = "text-emerald-600 font-bold";
                } else if (coinAmount < 0) {
                    coinAmountDisplay = COIN_FORMAT.format(coinAmount) + " Coin";
                    coinAmountClass = "text-rose-600 font-bold";
                } else {
                    coinAmountDisplay = "0 Coin";
                    coinAmountClass = "text-[#75695d]";
                }

                amountVndDisplay = "—";
                bankCode = "—";
                statusDisplay = "Đã ghi sổ";
                statusBadgeClass = "bg-[#e8f7ee] text-[#1b7a42] border border-[#b8e8cb]";
            }

            return TransactionAuditDTO.builder()
                    .sourceType(sourceType)
                    .recordId(recordId)
                    .orderCode(oc != null ? oc : "—")
                    .displayCode(displayCode)
                    .userId(userId)
                    .userName(userName)
                    .userEmail(userEmail)
                    .userAvatar(userAvatar)
                    .type(txType)
                    .typeDisplay(typeDisplay)
                    .typeBadgeClass(typeBadgeClass)
                    .detail(detail != null ? detail : "—")
                    .coinAmount(coinAmount)
                    .coinAmountDisplay(coinAmountDisplay)
                    .coinAmountClass(coinAmountClass)
                    .amountVnd(amountVnd)
                    .amountVndDisplay(amountVndDisplay)
                    .bankTransactionCode(bankCode != null && !bankCode.isBlank() ? bankCode : "—")
                    .status(status)
                    .statusDisplay(statusDisplay)
                    .statusBadgeClass(statusBadgeClass)
                    .time(txTime)
                    .formattedTime(DateTimeUtils.formatUtcToHanoi(txTime, "HH:mm dd/MM/yyyy"))
                    .hasIpnLog(hasIpnLog)
                    .build();
        });

        return new PageImpl<>(list, PageRequest.of(page, size), totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentLogDTO> getIpnLogsByOrderCode(String orderCode) {
        if (orderCode == null || orderCode.trim().isEmpty()) {
            return Collections.emptyList();
        }

        PaymentLog probe = PaymentLog.builder().orderCode(orderCode.trim()).build();
        List<PaymentLog> logs = paymentLogRepository.findAll(
                Example.of(probe),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        List<PaymentLogDTO> dtos = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (PaymentLog logItem : logs) {
            String pretty = logItem.getRawPayload();
            if (pretty != null && !pretty.isBlank()) {
                try {
                    Object json = objectMapper.readValue(pretty, Object.class);
                    pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                } catch (Exception e) {
                    // Raw string fallback
                    pretty = logItem.getRawPayload();
                }
            } else {
                pretty = "(Trống)";
            }

            dtos.add(PaymentLogDTO.builder()
                    .id(logItem.getId())
                    .orderCode(logItem.getOrderCode())
                    .rawPayload(logItem.getRawPayload())
                    .prettyPayload(pretty)
                    .ipAddress(logItem.getIpAddress())
                    .status(logItem.getStatus())
                    .createdAt(logItem.getCreatedAt())
                    .formattedCreatedAt(logItem.getCreatedAt() != null ? logItem.getCreatedAt().format(dtf) : "—")
                    .build());
        }

        return dtos;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String trimmed = dateStr.trim();
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy")
        );
        for (DateTimeFormatter dtf : formatters) {
            try {
                return LocalDate.parse(trimmed, dtf);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
