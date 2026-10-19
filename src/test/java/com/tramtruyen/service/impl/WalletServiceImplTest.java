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

import com.tramtruyen.dto.TransactionDTO;
import com.tramtruyen.repository.TopupOrderRepository;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    @Mock
    private TopupOrderRepository topupOrderRepository;

    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletServiceImpl(userRepository, transactionRepository, topupOrderRepository);
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

    @Test
    void searchTransactionsFiltersByDateCorrectly() {
        User user = User.builder().id(1).email("test@example.com").build();
        LocalDate targetDate = LocalDate.of(2026, 10, 19);
        LocalDateTime targetDateTime = targetDate.atTime(10, 30);
        LocalDateTime anotherDateTime = targetDate.minusDays(1).atTime(15, 0);

        Transaction tx1 = Transaction.builder()
                .id(1)
                .user(user)
                .amount(500)
                .balanceAfter(500)
                .type("DEPOSIT")
                .description("Nạp Coin")
                .createdAt(targetDateTime)
                .build();

        Transaction tx2 = Transaction.builder()
                .id(2)
                .user(user)
                .amount(-30)
                .balanceAfter(470)
                .type("UNLOCK_CHAPTER")
                .description("Mở khóa chương")
                .createdAt(anotherDateTime)
                .build();

        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(1))
                .thenReturn(List.of(tx1, tx2));
        when(topupOrderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1, "FAILED"))
                .thenReturn(List.of());

        Page<TransactionDTO> result = walletService.searchTransactions(user, "ALL", null, targetDate, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1);
    }

    @Test
    void searchTransactionsMapsGiftCoinAsPositiveReward() {
        User user = User.builder().id(1).email("test@example.com").build();
        Transaction checkinTx = Transaction.builder()
                .id(10)
                .user(user)
                .amount(5)
                .balanceAfter(2280)
                .type("GIFT_COIN")
                .description("Điểm danh hàng ngày (Chuỗi 1 ngày, mốc +5 Coin)")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(1))
                .thenReturn(List.of(checkinTx));
        when(topupOrderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1, "FAILED"))
                .thenReturn(List.of());

        Page<TransactionDTO> result = walletService.searchTransactions(user, "ALL", null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        TransactionDTO dto = result.getContent().get(0);
        assertThat(dto.getAmountDisplay()).isEqualTo("+ 5 Coin");
        assertThat(dto.getAmountColorClass()).contains("15803D");
        assertThat(dto.getIcon()).isEqualTo("featured_seasonal_and_gifts");
        assertThat(dto.getTitle()).contains("Điểm danh hàng ngày");
        assertThat(dto.getSecondaryText()).isEqualTo("Miễn phí");
    }
}
