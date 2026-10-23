package com.tramtruyen.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tramtruyen.dto.ReportCommentRequest;
import com.tramtruyen.dto.ReportContentRequest;
import com.tramtruyen.dto.ResolveReportRequest;
import com.tramtruyen.entity.*;
import com.tramtruyen.repository.*;
import com.tramtruyen.service.NotificationService;
import com.tramtruyen.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private static final Set<String> VALID_COMMENT_VIOLATIONS = Set.of(
            "SPAM", "HARASSMENT", "HATE_SPEECH", "SPOILER", "OTHER"
    );

    private static final Set<String> VALID_CONTENT_ISSUES = Set.of(
            "CHAPTER_ERROR", "TYPO_FORMAT", "WRONG_INFO", "INAPPROPRIATE", "COPYRIGHT"
    );

    private final CommentRepository commentRepository;
    private final CommentReportRepository commentReportRepository;
    private final ChapterRepository chapterRepository;
    private final ContentReportRepository contentReportRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SystemAuditLogRepository systemAuditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void reportComment(Integer userId, ReportCommentRequest request) {
        if (request == null || request.getCommentId() == null) {
            throw new IllegalArgumentException("Thông tin bình luận báo cáo không hợp lệ");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản người dùng"));

        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("Bình luận không còn khả dụng"));

        if (!"VISIBLE".equalsIgnoreCase(comment.getStatus())) {
            throw new IllegalArgumentException("Bình luận không khả dụng");
        }

        String violationType = request.getViolationType() != null ? request.getViolationType().trim().toUpperCase() : "";
        if (!VALID_COMMENT_VIOLATIONS.contains(violationType)) {
            throw new IllegalArgumentException("Loại vi phạm không hợp lệ");
        }

        String description = request.getDescription() != null ? request.getDescription().trim() : "";
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Mô tả lý do không được để trống");
        }
        if (description.length() < 10) {
            throw new IllegalArgumentException("Chi tiết lý do phải từ 10 đến 500 ký tự");
        }
        if (description.length() > 500) {
            throw new IllegalArgumentException("Mô tả lý do không được vượt quá 500 ký tự");
        }

        // Check duplicate report
        boolean alreadyReported = commentReportRepository.existsByUserIdAndCommentIdAndStatusIn(
                userId, comment.getId(), List.of("RECEIVED")
        );
        if (alreadyReported) {
            throw new IllegalStateException("Bạn đã gửi báo cáo cho bình luận này và đang chờ xử lý");
        }

        String compositeReason = "[" + violationType + "] " + description;

        CommentReport report = CommentReport.builder()
                .user(user)
                .comment(comment)
                .reason(compositeReason)
                .status("RECEIVED")
                .build();

        commentReportRepository.save(report);
    }

    @Override
    @Transactional
    public void reportContent(Integer userId, ReportContentRequest request) {
        if (request == null || request.getChapterId() == null) {
            throw new IllegalArgumentException("Thông tin chương báo cáo không hợp lệ");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản người dùng"));

        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nội dung chương"));

        if (Boolean.TRUE.equals(chapter.getIsDeleted()) || "DRAFT".equalsIgnoreCase(chapter.getStatus())) {
            throw new IllegalArgumentException("Chương không còn khả dụng để báo cáo");
        }

        String issueType = request.getIssueType() != null ? request.getIssueType().trim().toUpperCase() : "";
        if (!VALID_CONTENT_ISSUES.contains(issueType)) {
            throw new IllegalArgumentException("Loại vấn đề không hợp lệ");
        }

        String description = request.getDescription() != null ? request.getDescription().trim() : "";
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Mô tả chi tiết không được để trống");
        }
        if (description.length() < 10) {
            throw new IllegalArgumentException("Chi tiết vấn đề phải từ 10 đến 500 ký tự");
        }
        if (description.length() > 500) {
            throw new IllegalArgumentException("Mô tả chi tiết không được vượt quá 500 ký tự");
        }

        // Check duplicate
        boolean alreadyReported = contentReportRepository.existsByUserIdAndChapterIdAndStatusIn(
                userId, chapter.getId(), List.of("RECEIVED")
        );
        if (alreadyReported) {
            throw new IllegalStateException("Vấn đề này đang được xem xét");
        }

        String compositeReason = "[" + issueType + "] " + description;

        ContentReport report = ContentReport.builder()
                .user(user)
                .chapter(chapter)
                .reason(compositeReason)
                .status("RECEIVED")
                .build();

        contentReportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentReport> getCommentReports(String status, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = (size < 1 || size > 50) ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<CommentReport> result;
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            result = commentReportRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            result = commentReportRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase(), pageable);
        }

        if (safePage >= result.getTotalPages() && result.getTotalPages() > 0) {
            int clampedPage = result.getTotalPages() - 1;
            Pageable clampedPageable = PageRequest.of(clampedPage, safeSize);
            if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
                result = commentReportRepository.findAllByOrderByCreatedAtDesc(clampedPageable);
            } else {
                result = commentReportRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase(), clampedPageable);
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentReport> getContentReports(String status, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = (size < 1 || size > 50) ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<ContentReport> result;
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            result = contentReportRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            result = contentReportRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase(), pageable);
        }

        if (safePage >= result.getTotalPages() && result.getTotalPages() > 0) {
            int clampedPage = result.getTotalPages() - 1;
            Pageable clampedPageable = PageRequest.of(clampedPage, safeSize);
            if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
                result = contentReportRepository.findAllByOrderByCreatedAtDesc(clampedPageable);
            } else {
                result = contentReportRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase(), clampedPageable);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void resolveCommentReport(Integer adminId, Integer reportId, ResolveReportRequest request, String clientIp) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu xử lý không hợp lệ");
        }

        CommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi báo cáo"));

        if (!"RECEIVED".equalsIgnoreCase(report.getStatus())) {
            throw new IllegalStateException("Báo cáo đã được xử lý trước đó");
        }

        String note = request.getAdminNote() != null ? request.getAdminNote().trim() : "";
        if (note.isEmpty()) {
            throw new IllegalArgumentException("Ghi chú xử lý / lý do từ chối không được để trống");
        }
        if (note.length() > 500) {
            throw new IllegalArgumentException("Ghi chú không được vượt quá 500 ký tự");
        }

        User admin = userRepository.findById(adminId)
                .orElse(null);

        boolean isRejected = "REJECTED".equalsIgnoreCase(request.getDecision());
        String newStatus = isRejected ? "REJECTED" : "PROCESSED";
        String actionApplied = request.getAction() != null ? request.getAction() : "NONE";

        Comment comment = report.getComment();
        String oldCommentStatus = (comment != null) ? comment.getStatus() : "UNKNOWN";

        int affectedReplyCount = 0;
        List<Integer> affectedReplyIds = new ArrayList<>();

        if (!isRejected && comment != null) {
            boolean isHide = "HIDE".equalsIgnoreCase(actionApplied);
            boolean isDelete = "DELETE".equalsIgnoreCase(actionApplied);

            if (isHide || isDelete) {
                String targetStatus = isDelete ? "DELETED" : "HIDDEN";
                LocalDateTime now = LocalDateTime.now();

                // 1. Cập nhật bình luận gốc bị báo cáo
                comment.setStatus(targetStatus);
                comment.setUpdatedAt(now);
                commentRepository.save(comment);

                // 2. Duyệt theo tầng (BFS) tìm toàn bộ bình luận con cháu
                // Dùng tập visited chống lặp/vòng lặp, giới hạn độ sâu 50
                Set<Integer> visited = new HashSet<>();
                visited.add(comment.getId());
                List<Integer> currentLayer = Collections.singletonList(comment.getId());
                List<Comment> allDescendants = new ArrayList<>();
                int depth = 0;
                final int MAX_DEPTH = 50;

                while (!currentLayer.isEmpty() && depth < MAX_DEPTH) {
                    List<Comment> children = commentRepository.findByParentIdIn(currentLayer);
                    List<Integer> nextLayer = new ArrayList<>();
                    for (Comment child : children) {
                        if (child != null && child.getId() != null && visited.add(child.getId())) {
                            allDescendants.add(child);
                            nextLayer.add(child.getId());
                        }
                    }
                    currentLayer = nextLayer;
                    depth++;
                }

                affectedReplyCount = allDescendants.size();

                // 3. Quy tắc thăng hạng: VISIBLE < HIDDEN < DELETED (chỉ nâng, không hạ)
                // - Nếu targetStatus là DELETED: nâng mọi con có status != DELETED lên DELETED (con đã DELETED giữ DELETED)
                // - Nếu targetStatus là HIDDEN: nâng mọi con có status == VISIBLE lên HIDDEN (con đã DELETED giữ DELETED, con đã HIDDEN giữ HIDDEN)
                List<Integer> idsToUpdate = new ArrayList<>();
                for (Comment c : allDescendants) {
                    if (isDelete) {
                        if (!"DELETED".equalsIgnoreCase(c.getStatus())) {
                            idsToUpdate.add(c.getId());
                        }
                    } else if (isHide) {
                        if ("VISIBLE".equalsIgnoreCase(c.getStatus())) {
                            idsToUpdate.add(c.getId());
                        }
                    }
                }

                if (!idsToUpdate.isEmpty()) {
                    commentRepository.updateStatusForIds(idsToUpdate, targetStatus, now);
                }

                // Ghi nhận danh sách ID bị ảnh hưởng (cắt tối đa 50)
                affectedReplyIds = allDescendants.stream()
                        .map(Comment::getId)
                        .limit(50)
                        .collect(Collectors.toList());
            }
        }

        report.setStatus(newStatus);
        report.setAdminNote(note);
        report.setResolver(admin);
        report.setProcessedAt(LocalDateTime.now());
        commentReportRepository.save(report);

        // Send feedback notification to reporter
        if (report.getUser() != null) {
            String reporterName = report.getUser().getFullName();
            String commentSnippet = (comment != null && comment.getContent() != null)
                    ? (comment.getContent().length() > 60 ? comment.getContent().substring(0, 57) + "..." : comment.getContent())
                    : "bình luận";

            String notifTitle = isRejected
                    ? NotificationService.REPORT_NOTIFICATION_PREFIX + ": Từ chối báo cáo bình luận #RP-" + report.getId()
                    : NotificationService.REPORT_NOTIFICATION_PREFIX + ": Bình luận đã được xử lý #RP-" + report.getId();

            String repliesSuffix = affectedReplyCount > 0 ? " và " + affectedReplyCount + " câu trả lời bên dưới" : "";
            String actionText = isRejected ? "Từ chối tiếp nhận" : ("HIDE".equalsIgnoreCase(actionApplied) ? "Đã ẩn bình luận vi phạm" + repliesSuffix : "Đã gỡ bỏ bình luận vi phạm" + repliesSuffix);

            String notifContent = "Kính gửi bạn " + reporterName + ",\n\n"
                    + "Ban Quản Trị Trạm Truyện xin chân thành cảm ơn bạn đã gửi báo cáo về bình luận: \"" + commentSnippet + "\".\n\n"
                    + "• Kết quả thẩm duyệt: " + (isRejected ? "Từ chối báo cáo" : "Đã xử lý vi phạm") + "\n"
                    + "• Biện pháp áp dụng: " + actionText + "\n"
                    + "• Ghi chú phản hồi: " + note + "\n\n"
                    + "Chúc bạn có những giây phút đọc truyện thư thái và văn minh tại Trạm Truyện!";

            notificationRepository.save(Notification.builder()
                    .userId(report.getUser().getId())
                    .title(notifTitle)
                    .content(notifContent)
                    .isRead(false)
                    .build());
        }

        // Audit Log
        try {
            Map<String, Object> oldVal = Map.of("status", "RECEIVED", "commentStatus", oldCommentStatus);
            Map<String, Object> newVal = new LinkedHashMap<>();
            newVal.put("status", newStatus);
            newVal.put("action", actionApplied);
            newVal.put("adminNote", note);
            if (!isRejected && affectedReplyCount > 0) {
                newVal.put("affectedReplyCount", affectedReplyCount);
                newVal.put("affectedReplyIds", affectedReplyIds);
            }

            systemAuditLogRepository.save(SystemAuditLog.builder()
                    .adminId(adminId != null ? adminId : 1)
                    .actionType(isRejected ? "REJECT_COMMENT_REPORT" : "RESOLVE_COMMENT_REPORT")
                    .targetEntity("comment_reports")
                    .targetId(report.getId())
                    .oldValue(objectMapper.writeValueAsString(oldVal))
                    .newValue(objectMapper.writeValueAsString(newVal))
                    .ipAddress(clientIp)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi ghi audit log xử lý báo cáo bình luận", e);
        }
    }

    @Override
    @Transactional
    public void resolveContentReport(Integer adminId, Integer reportId, ResolveReportRequest request, String clientIp) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu xử lý không hợp lệ");
        }

        ContentReport report = contentReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi báo cáo"));

        if (!"RECEIVED".equalsIgnoreCase(report.getStatus())) {
            throw new IllegalStateException("Báo cáo đã được xử lý trước đó");
        }

        String note = request.getAdminNote() != null ? request.getAdminNote().trim() : "";
        if (note.isEmpty()) {
            throw new IllegalArgumentException("Ghi chú xử lý / lý do từ chối không được để trống");
        }
        if (note.length() > 500) {
            throw new IllegalArgumentException("Ghi chú không được vượt quá 500 ký tự");
        }

        User admin = userRepository.findById(adminId)
                .orElse(null);

        boolean isRejected = "REJECTED".equalsIgnoreCase(request.getDecision());
        String newStatus = isRejected ? "REJECTED" : "PROCESSED";
        String actionApplied = request.getAction() != null ? request.getAction() : "NONE";

        Chapter chapter = report.getChapter();
        String oldChapterStatus = (chapter != null) ? chapter.getStatus() : "UNKNOWN";

        if (!isRejected && chapter != null) {
            if ("DRAFT_CHAPTER".equalsIgnoreCase(actionApplied)) {
                chapter.setStatus("DRAFT");
                chapterRepository.save(chapter);
            }
        }

        report.setStatus(newStatus);
        report.setAdminNote(note);
        report.setResolver(admin);
        report.setProcessedAt(LocalDateTime.now());
        contentReportRepository.save(report);

        // Send feedback notification to reporter
        if (report.getUser() != null) {
            String reporterName = report.getUser().getFullName();
            String chapterTitle = (chapter != null)
                    ? ("Chương " + chapter.getChapterNumber() + " (" + (chapter.getNovel() != null ? chapter.getNovel().getTitle() : "") + ")")
                    : "chương truyện";

            String notifTitle = isRejected
                    ? NotificationService.REPORT_NOTIFICATION_PREFIX + ": Từ chối báo cáo nội dung #RP-" + report.getId()
                    : NotificationService.REPORT_NOTIFICATION_PREFIX + ": Đã xử lý nội dung " + chapterTitle + " #RP-" + report.getId();

            String actionText = isRejected ? "Từ chối tiếp nhận" : ("DRAFT_CHAPTER".equalsIgnoreCase(actionApplied) ? "Tạm ẩn chương để biên tập lại" : "Chỉ ghi nhận, không thay đổi nội dung");

            String notifContent = "Kính gửi bạn " + reporterName + ",\n\n"
                    + "Ban Quản Trị Trạm Truyện xin chân thành cảm ơn đóng góp của bạn về sự cố nội dung tại " + chapterTitle + ".\n\n"
                    + "• Kết quả thẩm duyệt: " + (isRejected ? "Từ chối báo cáo" : "Đã xử lý nội dung") + "\n"
                    + "• Biện pháp khắc phục: " + actionText + "\n"
                    + "• Ghi chú phản hồi: " + note + "\n\n"
                    + "Cảm ơn bạn đã đồng hành giữ gìn không gian đọc văn trọn vẹn nhất cho cộng đồng độc giả!";

            notificationRepository.save(Notification.builder()
                    .userId(report.getUser().getId())
                    .title(notifTitle)
                    .content(notifContent)
                    .isRead(false)
                    .build());
        }

        // Audit Log
        try {
            Map<String, Object> oldVal = Map.of("status", "RECEIVED", "chapterStatus", oldChapterStatus);
            Map<String, Object> newVal = Map.of("status", newStatus, "action", actionApplied, "adminNote", note);

            systemAuditLogRepository.save(SystemAuditLog.builder()
                    .adminId(adminId != null ? adminId : 1)
                    .actionType(isRejected ? "REJECT_CONTENT_REPORT" : "RESOLVE_CONTENT_REPORT")
                    .targetEntity("content_reports")
                    .targetId(report.getId())
                    .oldValue(objectMapper.writeValueAsString(oldVal))
                    .newValue(objectMapper.writeValueAsString(newVal))
                    .ipAddress(clientIp)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi ghi audit log xử lý báo cáo nội dung", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getReportCounts() {
        long pendingComments = commentReportRepository.countByStatus("RECEIVED");
        long pendingContents = contentReportRepository.countByStatus("RECEIVED");
        return Map.of(
                "pendingComments", pendingComments,
                "pendingContents", pendingContents,
                "totalPending", pendingComments + pendingContents
        );
    }
}
