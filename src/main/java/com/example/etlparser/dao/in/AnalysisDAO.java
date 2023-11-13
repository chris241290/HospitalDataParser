package com.example.etlparser.dao.in;

import com.example.etlparser.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisDAO extends JpaRepository<Analysis,Long> {
}
