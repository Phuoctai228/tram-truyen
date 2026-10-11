package com.tramtruyen.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @org.hibernate.annotations.Formula("(SELECT COUNT(*) FROM novel_categories nc JOIN novels n ON nc.novel_id = n.id WHERE nc.category_id = id AND n.is_deleted = false)")
    private Integer novelCount;
}
