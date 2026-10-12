package com.tramtruyen.service;

import com.tramtruyen.dto.ReportCommentRequest;
import com.tramtruyen.dto.ReportContentRequest;
import com.tramtruyen.dto.ResolveReportRequest;
import com.tramtruyen.entity.CommentReport;
import com.tramtruyen.entity.ContentReport;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface ReportService {

    void reportComment(Integer userId, ReportCommentRequest request);

    void reportContent(Integer userId, ReportContentRequest request);

    Page<CommentReport> getCommentReports(String status, int page, int size);

    Page<ContentReport> getContentReports(String status, int page, int size);

    void resolveCommentReport(Integer adminId, Integer reportId, ResolveReportRequest request, String clientIp);

    void resolveContentReport(Integer adminId, Integer reportId, ResolveReportRequest request, String clientIp);

    Map<String, Long> getReportCounts();
}
