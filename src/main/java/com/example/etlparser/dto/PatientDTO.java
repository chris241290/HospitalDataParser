package com.example.etlparser.dto;

import com.example.etlparser.entity.Gender;
import com.example.etlparser.entity.Hospitalization;
import com.example.etlparser.entity.Patient;
import com.example.etlparser.entity.Rural;
import lombok.*;

import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
public class PatientDTO extends Patient {
    public PatientDTO(Long serialNumber, Integer age, Gender gender, Rural rural, Boolean smoking, Boolean alcohol, Boolean diabetesMellitus, Boolean hypertension, Boolean coronaryArteryDisease, Boolean cardiomyopahty, Boolean chronicKidneyDisease, List<Hospitalization> lista) {
        super(serialNumber, age, gender, rural, smoking, alcohol, diabetesMellitus, hypertension, coronaryArteryDisease, cardiomyopahty, chronicKidneyDisease, lista);
    }

    public Patient toEntity(){
        return new Patient(super.getSerialNumber(),super.getAge(),super.getGender(),super.getRural(),super.getSmoking(),super.getAlcohol(),super.getDiabetesMellitus(),super.getHypertension(),super.getCoronaryArteryDisease(),super.getCardiomyopahty(),super.getChronicKidneyDisease(),super.getLista());
    }

}
