package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT r FROM Report r " +
           "LEFT JOIN FETCH r.post p " +
           "LEFT JOIN FETCH p.user " +
           "LEFT JOIN FETCH r.reporter " +
           "WHERE r.status = :status " +
           "ORDER BY r.createdAt DESC")
    List<Report> findByStatusOrderByCreatedAtDesc(@Param("status") String status);
}