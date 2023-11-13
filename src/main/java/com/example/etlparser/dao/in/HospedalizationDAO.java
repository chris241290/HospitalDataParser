package com.example.etlparser.dao.in;

import com.example.etlparser.entity.Hospitalization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospedalizationDAO extends JpaRepository<Hospitalization,Long> {
}
