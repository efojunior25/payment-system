package com.xunim.paymentsystem.repository.mysql;

import com.xunim.paymentsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByDocument(String document);

    boolean existsByEmail(String email);

    boolean existsByDocument(String document);

    List<User> findByIsActiveTrue();

    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);

    @Query("SELECT u FROM User u JOIN FETCH u.accounts WHERE u.id = :id")
    Optional<User> findByIdWithAccounts(@Param("id") Long id);
}