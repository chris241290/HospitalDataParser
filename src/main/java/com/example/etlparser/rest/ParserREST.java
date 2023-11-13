package com.example.etlparser.rest;

import com.example.etlparser.dto.ResultDTO;
import com.example.etlparser.entity.*;
import com.example.etlparser.service.in.ParserService;
import com.example.etlparser.utils.Utils;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.javapoet.ClassName;
import org.springframework.web.bind.annotation.*;
import com.opencsv.CSVReader;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

// un servizio REST in Java utilizzando Spring Boot. Gestisce l'upload di file CSV contenenti dati relativi a pazienti e le loro ospedalizzazioni. 
// I dati vengono poi processati e normalizzati prima di essere caricati in un database tramite un servizio ParserService. 
// Di seguito un riassunto delle funzionalità principali:
//
// Upload di file CSV: 

// L'endpoint REST /parser/uploadCSV accetta file CSV come parte di una richiesta multipart.
// Il file viene quindi processato per estrarre e normalizzare i dati.
//
// Validazione del formato del file CSV: 

// Prima di procedere con il parsing, viene verificato se il file è un CSV valido.
//
// Parsing dei dati:

// I dati CSV vengono letti e normalizzati. 
// Viene eseguita una serie di controlli, conversioni di tipo e creazione di oggetti Java 
// per rappresentare i pazienti, le analisi e le ospedalizzazioni.

//Logging:

// Viene utilizzato il modulo di logging di Java (java.util.logging) per registrare informazioni
// dettagliate durante l'esecuzione.


@RestController
@RequestMapping(value="/parser")
public class ParserREST {

    @Autowired
    private ParserService service;

    private static final Logger LOGGER = Logger.getLogger(ClassName.class.getName());

    @Value("${folderCount}")
    private Integer folderCount;

    @PostMapping(value = "/uploadCVS",consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE
    })
    public ResponseEntity<ResultDTO<Integer>> uploadCSV(@RequestPart("file")MultipartFile file) {
        ResultDTO<Integer> res=new ResultDTO<>();
        res.setFailureResponse("Errore generico ",500);
        boolean isFirstLine = true;
        ArrayList<String> defaultHeader = new ArrayList<>(List.of("SNO", "MRD No.", "D.O.A", "D.O.D", "AGE", "GENDER", "RURAL", "TYPE OF ADMISSION-EMERGENCY/OPD", "month year", "DURATION OF STAY", "duration of intensive unit stay", "OUTCOME", "SMOKING", "ALCOHOL", "DM", "HTN", "CAD", "PRIOR CMP", "CKD", "HB", "TLC", "PLATELETS", "GLUCOSE", "UREA", "CREATININE", "BNP", "RAISED CARDIAC ENZYMES", "EF", "SEVERE ANAEMIA", "ANAEMIA", "STABLE ANGINA", "ACS", "STEMI", "ATYPICAL CHEST PAIN", "HEART FAILURE", "HFREF", "HFNEF", "VALVULAR", "CHB", "SSS", "AKI", "CVA INFRACT", "CVA BLEED", "AF", "VT", "PSVT", "CONGENITAL", "UTI", "NEURO CARDIOGENIC SYNCOPE", "ORTHOSTATIC", "INFECTIVE ENDOCARDITIS", "DVT", "CARDIOGENIC SHOCK", "SHOCK", "PULMONARY EMBOLISM", "CHEST INFECTION"));
        if (folderCount==null)
            folderCount=0;
        Path uploadPath = Paths.get("FileUpload"+folderCount);
        folderCount++;
        while (Files.exists(uploadPath)) {
            folderCount++;
            uploadPath = Paths.get("FileUpload"+folderCount);
        }
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            LOGGER.info("Errore creazione nuova directory");
        }
        File csv=null;
        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(file.getOriginalFilename());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            csv=new File(file.getOriginalFilename());
            file.transferTo(csv);
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO,"errore");
        }
        Integer cont=0;
        if (csv!=null&&Utils.isCSV(csv)){
            String[] nextLine;
            File temp=new File(uploadPath+"/"+csv.getPath());
            try (CSVReader reader = new CSVReader(new FileReader(temp))){
                while ((nextLine = reader.readNext())!= null){
                    if(isFirstLine){
                        isFirstLine = false;
                        ArrayList<String> header = new ArrayList<>(List.of(nextLine));
                        if(!Utils.isEqualHeaderList(header,defaultHeader)){
                            res.setFailureResponse("Colonne non corrispondenti ",500);
                            LOGGER.log(Level.FINE,defaultHeader+"\n"+header);
                            throw new Exception("Colonne non corrispondenti");
                        }
                    }else{
                        String e = "EMPTY";
                        HashMap<String, String> dict = new HashMap<>();
                        dict.put("hb", nextLine[19]);
                        dict.put("tlc", nextLine[20]);
                        dict.put("platelets", nextLine[21]);
                        dict.put("glucose", nextLine[22]);
                        dict.put("urea", nextLine[23]);
                        dict.put("creatinine", nextLine[24]);
                        dict.put("bnp", nextLine[25]);
                        dict.put("raised_cardiac_enzymes", nextLine[26]);
                        dict.put("ef", nextLine[27]);
                        int counter = 0;
                        for(String s:dict.values()){
                            if(s == null || s.equals(e))
                                counter++;
                        }
                        if(counter>2) {
                            LOGGER.log(Level.FINE,"Tupla scartata, troppe colonne vuote nelle analisi");
                            continue;
                        }
                        Analysis analisi = new Analysis();
                        try {
                            analisi.setHaemoglobin(Double.valueOf(dict.get("hb")));
                        }catch (NumberFormatException e1){
                            analisi.setHaemoglobin(null);
                        }
                        try {
                            analisi.setTotalLeukocytesCount(Double.valueOf(dict.get("tlc")));
                        }catch (NumberFormatException e1){
                            analisi.setTotalLeukocytesCount(null);
                        }
                        try {
                            analisi.setPalatelets(Integer.valueOf(dict.get("platelets")));
                        }catch (NumberFormatException e1){
                            analisi.setPalatelets(null);
                        }
                        try {
                            analisi.setGlucose(Integer.valueOf(dict.get("glucose")));
                        }catch (NumberFormatException e1){
                            analisi.setGlucose(null);
                        }
                        try {
                            analisi.setUrea(Integer.valueOf(dict.get("urea")));
                        }catch (NumberFormatException e1){
                            analisi.setUrea(null);
                        }
                        try {
                            analisi.setCreatinine(Double.valueOf(dict.get("creatinine")));
                        }catch (NumberFormatException e1){
                            analisi.setCreatinine(null);
                        }
                        try {
                            analisi.setBTypeNatriureticPeptide(Integer.valueOf(dict.get("bnp")));
                        }catch (NumberFormatException e1){
                            analisi.setBTypeNatriureticPeptide(null);
                        }
                        try {
                            analisi.setRaisedCardiacEnzymes(Boolean.valueOf(dict.get("raised_cardiac_enzymes")));
                        }catch (NumberFormatException e1){
                            analisi.setRaisedCardiacEnzymes(null);
                        }
                        try {
                            analisi.setEjectionFraction(Integer.valueOf(dict.get("ef")));
                        }catch (NumberFormatException e1){
                            analisi.setEjectionFraction(null);
                        }
                        Long serialNumber=null;
                        Integer age=null;
                        try{
                            serialNumber=Long.valueOf(nextLine[1]);
                        }catch (NumberFormatException e3){
                            res.setFailureResponse("Errore lanciata eccezzione"+e3,500);
                            LOGGER.log(Level.FINE,"errore seriale number");
                        }
                        try{
                            age=Integer.valueOf(nextLine[4]);
                        }catch (NumberFormatException e3){
                            LOGGER.log(Level.FINE,"errore era ");
                        }
                        Patient paziente=null;
                        if(serialNumber!=null)  paziente= new Patient(serialNumber,age,
                                Gender.valueOf(nextLine[5]),Rural.valueOf(nextLine[6]),Boolean.valueOf(nextLine[12]),
                                Boolean.valueOf(nextLine[13]),Boolean.valueOf(nextLine[14]),Boolean.valueOf(nextLine[15]),
                                Boolean.valueOf(nextLine[16]),Boolean.valueOf(nextLine[17]),Boolean.valueOf(nextLine[18]),
                                new ArrayList<>());
                        /*paziente.setSerialNumber(Long.valueOf(nextLine[1]));
                        paziente.setAge(Integer.valueOf(nextLine[4]));
                        paziente.setGender(Gender.valueOf(nextLine[5]));
                        paziente.setRural(Rural.valueOf(nextLine[6]));
                        paziente.setSmoking(Boolean.valueOf(nextLine[12]));
                        paziente.setAlcohol(Boolean.valueOf(nextLine[13]));
                        paziente.setDiabetesMellitus(Boolean.valueOf(nextLine[14]));
                        paziente.setHypertension(Boolean.valueOf(nextLine[15]));
                        paziente.setCoronaryArteryDisease(Boolean.valueOf(nextLine[16]));
                        paziente.setCardiomyopahty(Boolean.valueOf(nextLine[17]));
                        paziente.setChronicKidneyDisease(Boolean.valueOf(nextLine[18]));
                        paziente.setLista(new ArrayList<>());*/
                        if (paziente!=null) {
                            LOGGER.log(Level.FINE,"id " + nextLine[0] + "id " + nextLine[1]);
                            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("M/d/yyyy");
                            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("d/M/yyyy");
                            LocalDate inizio = null;
                            LocalDate fine = null;
                            try {
                                inizio = LocalDate.parse(nextLine[2], formatter1);
                                fine = LocalDate.parse(nextLine[3], formatter1);
                            } catch (DateTimeException e2) {
                                res.setFailureResponse("Errore lanciata eccezzione"+e2,500);
                                LOGGER.log(Level.FINE,"errore 1 date");
                            }
                            try {
                                inizio = LocalDate.parse(nextLine[2], formatter2);
                                fine = LocalDate.parse(nextLine[3], formatter2);
                            } catch (DateTimeException e2) {
                                res.setFailureResponse("Errore lanciata eccezzione"+e2,500);
                                LOGGER.log(Level.FINE,"errore 1 date");
                            }
                            Hospitalization ospedalizzazione = new Hospitalization(Long.valueOf(nextLine[0]), paziente, analisi,
                                    inizio, fine, TOA.valueOf(nextLine[7]),
                                    Integer.valueOf(nextLine[9]), Integer.valueOf(nextLine[10]), Outcame.valueOf(nextLine[11]),
                                    Boolean.valueOf(nextLine[28]), Boolean.valueOf(nextLine[29]), Boolean.valueOf(nextLine[30]),
                                    Boolean.valueOf(nextLine[31]), Boolean.valueOf(nextLine[32]), Boolean.valueOf(nextLine[33]),
                                    Boolean.valueOf(nextLine[34]), Boolean.valueOf(nextLine[35]), Boolean.valueOf(nextLine[36]),
                                    Boolean.valueOf(nextLine[37]), Boolean.valueOf(nextLine[38]), Boolean.valueOf(nextLine[39]),
                                    Boolean.valueOf(nextLine[40]), Boolean.valueOf(nextLine[41]), Boolean.valueOf(nextLine[42]),
                                    Boolean.valueOf(nextLine[43]), Boolean.valueOf(nextLine[44]), Boolean.valueOf(nextLine[45]),
                                    Boolean.valueOf(nextLine[46]), Boolean.valueOf(nextLine[47]), Boolean.valueOf(nextLine[48]),
                                    Boolean.valueOf(nextLine[49]), Boolean.valueOf(nextLine[50]), Boolean.valueOf(nextLine[51]),
                                    Boolean.valueOf(nextLine[52]), Boolean.valueOf(nextLine[53]), Boolean.valueOf(nextLine[54]),
                                    Boolean.valueOf(nextLine[55]));
                            /*ospedalizzazione.setAdmissionNumber(Long.valueOf(nextLine[0]));
                            ospedalizzazione.setPaziente(paziente);
                            ospedalizzazione.setAnalisi(analisi);
                            //Local data da controllare il parse
                            ospedalizzazione.setDateAdmission(LocalDate.parse(nextLine[2]));
                            ospedalizzazione.setDateDischarge(LocalDate.parse(nextLine[3]));
                            ospedalizzazione.setTypeOfAdmissionEmergencyOPD(TOA.valueOf(nextLine[7]));
                            ospedalizzazione.setDurationOfStay(Integer.valueOf(nextLine[9]));
                            ospedalizzazione.setDurationOfIntensiveUnitStay(Integer.valueOf(nextLine[10]));
                            ospedalizzazione.setOutcame(Outcame.valueOf(nextLine[11]));
                            ospedalizzazione.setSevereAnemia(Boolean.valueOf(nextLine[28]));
                            ospedalizzazione.setAnemia(Boolean.valueOf(nextLine[29]));
                            ospedalizzazione.setStableAngina(Boolean.valueOf(nextLine[30]));
                            ospedalizzazione.setAcuteCoronarySyndrome(Boolean.valueOf(nextLine[31]));
                            ospedalizzazione.setStElevationMyocardialInfraction(Boolean.valueOf(nextLine[32]));
                            ospedalizzazione.setAtypicalChestPain(Boolean.valueOf(nextLine[33]));
                            ospedalizzazione.setHeartFailure(Boolean.valueOf(nextLine[34]));
                            ospedalizzazione.setHeartFailureWithReducedFraction(Boolean.valueOf(nextLine[35]));
                            ospedalizzazione.setHeartFailureWithNormalFraction(Boolean.valueOf(nextLine[36]));
                            ospedalizzazione.setValvularHeartDisease(Boolean.valueOf(nextLine[37]));
                            ospedalizzazione.setCompleteHeartBlock(Boolean.valueOf(nextLine[38]));
                            ospedalizzazione.setSickSinusSyndrome(Boolean.valueOf(nextLine[39]));
                            ospedalizzazione.setAcuteKidneyInjury(Boolean.valueOf(nextLine[40]));
                            ospedalizzazione.setCerebrovascularAccidentInfract(Boolean.valueOf(nextLine[41]));
                            ospedalizzazione.setCerebrovascularAccidentBleed(Boolean.valueOf(nextLine[42]));
                            ospedalizzazione.setAtrialFibrilation(Boolean.valueOf(nextLine[43]));
                            ospedalizzazione.setVentricularTachycardia(Boolean.valueOf(nextLine[44]));
                            ospedalizzazione.setParoxysmalSupraVentricularTachycardia(Boolean.valueOf(nextLine[45]));
                            ospedalizzazione.setCongenitalHeartDisease(Boolean.valueOf(nextLine[46]));
                            ospedalizzazione.setUrinaryTractInfection(Boolean.valueOf(nextLine[47]));
                            ospedalizzazione.setNeuroCardiogenicSyncope(Boolean.valueOf(nextLine[48]));
                            ospedalizzazione.setOrthostatic(Boolean.valueOf(nextLine[49]));
                            ospedalizzazione.setInfectiveEndocarditis(Boolean.valueOf(nextLine[50]));
                            ospedalizzazione.setDeepVenousThrombosis(Boolean.valueOf(nextLine[51]));
                            ospedalizzazione.setCardiogenicShock(Boolean.valueOf(nextLine[52]));
                            ospedalizzazione.setShock(Boolean.valueOf(nextLine[53]));
                            ospedalizzazione.setPulmonaryShock(Boolean.valueOf(nextLine[54]));
                            ospedalizzazione.setChestInfection(Boolean.valueOf(nextLine[55]));*/
                            paziente.getLista().add(ospedalizzazione);
                            service.uploadPatientHistory(paziente.toDTO());
                            cont++;
                        }
                        else {
                            LOGGER.log(Level.FINE,"Qualcosa è andato storto");
                        }
                    }
                }
                res.setSuccessTrueResponse("File csv normalizzato correttamente");
                res.setCode(HttpStatus.OK.value());
                res.setData(cont);
            }catch (IOException e) {
                res.setFailureResponse("Errore lanciata eccezzione"+e,500);
                throw new RuntimeException(e);
            } catch (CsvValidationException e) {
                res.setFailureResponse("Errore lanciata eccezzione"+e,500);
                throw new RuntimeException(e);
            } catch (Exception e) {
                res.setFailureResponse("Errore lanciata eccezzione"+e,500);
                throw new RuntimeException(e);
            }
        }
        return new ResponseEntity<>(res, HttpStatusCode.valueOf(res.getCode()));
    }


    }
