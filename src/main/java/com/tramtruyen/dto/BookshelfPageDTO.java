package com.tramtruyen.dto;

import com.tramtruyen.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data transfer object encapsulating the full bookshelf page data,
 * including items, category stats, filtering, sorting, and pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookshelfPageDTO {

    private List<BookshelfItemDTO> items;
    private long totalCount;
    private long readingCount;
    private long completedCount;
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private boolean hasPrevious;
    private boolean hasNext;
    private String currentFilter;
    private String searchQuery;
    private String currentSort;
    private List<Category> popularCategories;
}
