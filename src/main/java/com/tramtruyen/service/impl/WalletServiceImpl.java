package com.tramtruyen.service.impl;

import com.tramtruyen.entity.Transaction;
import com.tramtruyen.entity.User;
import com.tramtruyen.exception.InsufficientBalanceException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.TransactionRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Production implementation of WalletService managing coin balances with pessimistic locking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public Integer getBalance(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getWalletBalance() != null ? u.getWalletBalance() : 0)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    @Override
    @Transactional
    public void deductCoins(User user, int amount, String type, Integer referenceId, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số Coin cần trừ phải lớn hơn 0");
        }

        // Lock user record with PESSIMISTIC_WRITE (SELECT ... FOR UPDATE) to prevent race conditions
        User lockedUser = userRepository.findByIdWithLock(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        int currentBalance = lockedUser.getWalletBalance() != null ? lockedUser.getWalletBalance() : 0;
        if (currentBalance < amount) {
            throw new InsufficientBalanceException(
                    "Số dư Coin không đủ (Hiện có: " + currentBalance + " Coin, Cần: " + amount + " Coin)"
            );
        }

        int newBalance = currentBalance - amount;
        lockedUser.setWalletBalance(newBalance);
        userRepository.save(lockedUser);

        // Record in ledger
        Transaction tx = Transaction.builder()
                .user(lockedUser)
                .amount(-amount)
                .balanceAfter(newBalance)
                .type(type)
                .referenceId(referenceId)
                .description(description)
                .build();
        transactionRepository.save(tx);

        log.info("Deducted {} coins from user {}. New balance: {}", amount, lockedUser.getEmail(), newBalance);
    }

    @Override
    @Transactional
    public void addCoins(User user, int amount, String type, Integer referenceId, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số Coin cộng phải lớn hơn 0");
        }

        User lockedUser = userRepository.findByIdWithLock(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        int currentBalance = lockedUser.getWalletBalance() != null ? lockedUser.getWalletBalance() : 0;
        int newBalance = currentBalance + amount;
        lockedUser.setWalletBalance(newBalance);
        userRepository.save(lockedUser);

        Transaction tx = Transaction.builder()
                .user(lockedUser)
                .amount(amount)
                .balanceAfter(newBalance)
                .type(type)
                .referenceId(referenceId)
                .description(description)
                .build();
        transactionRepository.save(tx);

        log.info("Credited {} coins to user {}. New balance: {}", amount, lockedUser.getEmail(), newBalance);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(User user, int amount) {
        if (user == null) {
            return false;
        }
        int balance;
        if (user.getId() != null) {
            balance = userRepository.findById(user.getId())
                    .map(u -> u.getWalletBalance() != null ? u.getWalletBalance() : 0)
                    .orElseGet(() -> user.getWalletBalance() != null ? user.getWalletBalance() : 0);
        } else {
            balance = user.getWalletBalance() != null ? user.getWalletBalance() : 0;
        }
        return balance >= amount;
    }
}
