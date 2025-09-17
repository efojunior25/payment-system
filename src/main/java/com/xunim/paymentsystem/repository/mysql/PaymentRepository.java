package com.xunim.paymentsystem.repository.mysql;

import com.xunim.paymentsystem.entity.Payment;
import com.xunim.paymentsystem.enums.PaymentStatus;
import com.xunim.paymentsystem.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByFromAccountId(Long fromAccountId);

    List<Payment> findByToAccountId(Long toAccountId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByPaymentType(PaymentType paymentType);

    @Query("SELECT p FROM Payment p WHERE p.fromAccount.id = :accountId OR p.toAccount.id = :accountId")
    Page<Payment> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentType = :type")
    BigDecimal getTotalAmountByType(@Param("type") PaymentType type);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.createdAt >= :date")
    long countByStatusSince(@Param("status") PaymentStatus status, @Param("date") LocalDateTime date);
}