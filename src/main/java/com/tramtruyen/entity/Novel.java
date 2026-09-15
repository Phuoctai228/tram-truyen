package com.tramtruyen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "novels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Novel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "cover_url", length = 255)
    private String coverUrl;

    @Builder.Default
    @Column(length = 50)
    private String status = "ONGOING"; // ONGOING, COMPLETED, ON_HOLD, ARCHIVED

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "uploader_id")
    private Integer uploaderId;

    @Builder.Default
    private Integer views = 0;

    @Builder.Default
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
