package com.tramtruyen.service.impl;

import com.tramtruyen.entity.Transaction;
import com.tramtruyen.entity.User;
import com.tramtruyen.exception.InsufficientBalanceException;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.TransactionRepository;
import com.tramtruyen.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletServiceImpl(userRepository, transactionRepository);
    }

    @Test
    void getBalanceReturnsWalletBalance() {
        User user = User.builder().id(1).email("test@example.com").walletBalance(100).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        int balance = walletService.getBalance("test@example.com");

        assertThat(balance).isEqualTo(100);
    }

    @Test
    void getBalanceThrowsExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getBalance("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deductCoinsSuccessfullyUpdatesBalanceAndRecordsTransaction() {
        User user = User.builder().id(1).email("test@example.com").walletBalance(100).build();
        when(userRepository.findByIdWithLock(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        walletService.deductCoins(user, 30, "CHAPTER_UNLOCK", 123, "Mở khóa chương 15");

        assertThat(user.getWalletBalance()).isEqualTo(70);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction tx = txCaptor.getValue();
        assertThat(tx.getAmount()).isEqualTo(-30);
        assertThat(tx.getBalanceAfter()).isEqualTo(70);
        assertThat(tx.getType()).isEqualTo("CHAPTER_UNLOCK");
        assertThat(tx.getReferenceId()).isEqualTo(123);
    }

    @Test
    void deductCoinsThrowsInsufficientBalanceExceptionWhenNotEnoughCoins() {
        User user = User.builder().id(1).email("test@example.com").walletBalance(20).build();
        when(userRepository.findByIdWithLock(1)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> walletService.deductCoins(user, 50, "CHAPTER_UNLOCK", 123, "Mở khóa chương"))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Số dư Coin không đủ");

        verify(transactionRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addCoinsSuccessfullyAddsCoinsAndRecordsTransaction() {
        User user = User.builder().id(1).email("test@example.com").walletBalance(50).build();
        when(userRepository.findByIdWithLock(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        walletService.addCoins(user, 15, "DAILY_CHECKIN", 5, "Điểm danh ngày 5");

        assertThat(user.getWalletBalance()).isEqualTo(65);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction tx = txCaptor.getValue();
        assertThat(tx.getAmount()).isEqualTo(15);
        assertThat(tx.getBalanceAfter()).isEqualTo(65);
        assertThat(tx.getType()).isEqualTo("DAILY_CHECKIN");
    }

    @Test
    void hasSufficientBalanceReturnsTrueWhenBalanceIsGreaterOrEqual() {
        User user = User.builder().walletBalance(100).build();
        assertThat(walletService.hasSufficientBalance(user, 50)).isTrue();
        assertThat(walletService.hasSufficientBalance(user, 100)).isTrue();
        assertThat(walletService.hasSufficientBalance(user, 150)).isFalse();
    }
}
