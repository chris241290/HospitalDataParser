package com.example.etlparser.dao.in;

import com.example.etlparser.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientDAO extends JpaRepository<Patient,Long> {
}
