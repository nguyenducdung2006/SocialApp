package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {}