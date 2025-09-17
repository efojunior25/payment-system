package com.xunim.paymentsystem.repository.mysql;

import com.xunim.paymentsystem.entity.Account;
import com.xunim.paymentsystem.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserIdAndStatus(Long userId, AccountStatus status);

    List<Account> findByUserId(Long userId);

    List<Account> findByStatus(AccountStatus status);

    @Query("SELECT a FROM Account a WHERE a.balance >= :minBalance")
    List<Account> findAccountsWithMinimumBalance(@Param("minBalance") BigDecimal minBalance);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.id = :accountId")
    void addToBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance - :amount WHERE a.id = :accountId AND a.balance >= :amount")
    int subtractFromBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.status = 'ACTIVE'")
    BigDecimal getTotalActiveBalance();
}