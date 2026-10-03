package com.tramtruyen.service;

import com.tramtruyen.entity.Bookshelf;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;

import java.util.List;

public interface BookshelfService {
    
    List<Bookshelf> getUserBookshelf(String email);
    
    void addNovelToBookshelf(String email, Integer novelId);
    
    void removeNovelFromBookshelf(String email, Integer novelId);
    
    boolean isNovelInBookshelf(String email, Integer novelId);
}
