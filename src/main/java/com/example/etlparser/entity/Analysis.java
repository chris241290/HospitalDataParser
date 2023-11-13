package com.example.etlparser.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Data
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {

    @Id
    @GeneratedValue
    private Long id;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Hospitalization ospedalizzazione;
    private Double haemoglobin;
    private Double totalLeukocytesCount;
    private Integer palatelets;
    private Integer glucose;
    private Integer urea;
    private Double creatinine;
    private Integer bTypeNatriureticPeptide;
    private Boolean raisedCardiacEnzymes;
    private Integer ejectionFraction;
}
