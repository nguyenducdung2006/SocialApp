package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}