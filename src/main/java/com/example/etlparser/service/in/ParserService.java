package com.example.etlparser.service.in;

import com.example.etlparser.dto.PatientDTO;
import com.example.etlparser.entity.Patient;

public interface ParserService {

    public void uploadPatientHistory(PatientDTO paziente);
}
