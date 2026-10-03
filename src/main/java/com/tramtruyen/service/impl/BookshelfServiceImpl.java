package com.tramtruyen.service.impl;

import com.tramtruyen.entity.Bookshelf;
import com.tramtruyen.entity.Novel;
import com.tramtruyen.entity.User;
import com.tramtruyen.exception.DuplicateResourceException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.BookshelfRepository;
import com.tramtruyen.repository.NovelRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.BookshelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookshelfServiceImpl implements BookshelfService {

    private final BookshelfRepository bookshelfRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;

    @Override
    public List<Bookshelf> getUserBookshelf(String email) {
        User user = getUserByEmail(email);
        return bookshelfRepository.findAllByUserOrderByAddedAtDesc(user);
    }

    @Override
    @Transactional
    public void addNovelToBookshelf(String email, Integer novelId) {
        User user = getUserByEmail(email);
        Novel novel = getNovelById(novelId);
        
        if (bookshelfRepository.existsByUserAndNovel(user, novel)) {
            throw new DuplicateResourceException("Novel is already in your bookshelf");
        }
        
        Bookshelf bookshelf = Bookshelf.builder()
                .user(user)
                .novel(novel)
                .build();
                
        bookshelfRepository.save(bookshelf);
    }

    @Override
    @Transactional
    public void removeNovelFromBookshelf(String email, Integer novelId) {
        User user = getUserByEmail(email);
        Novel novel = getNovelById(novelId);
        
        Bookshelf bookshelf = bookshelfRepository.findByUserAndNovel(user, novel)
                .orElseThrow(() -> new ResourceNotFoundException("Novel not found in your bookshelf"));
                
        bookshelfRepository.delete(bookshelf);
    }

    @Override
    public boolean isNovelInBookshelf(String email, Integer novelId) {
        try {
            User user = getUserByEmail(email);
            Novel novel = getNovelById(novelId);
            return bookshelfRepository.existsByUserAndNovel(user, novel);
        } catch (Exception e) {
            return false;
        }
    }
    
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    private Novel getNovelById(Integer id) {
        return novelRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));
    }
}
