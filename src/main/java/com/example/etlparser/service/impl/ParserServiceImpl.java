package com.example.etlparser.service.impl;

import com.example.etlparser.dao.in.AnalysisDAO;
import com.example.etlparser.dao.in.HospedalizationDAO;
import com.example.etlparser.dao.in.PatientDAO;
import com.example.etlparser.dto.PatientDTO;
import com.example.etlparser.entity.Hospitalization;
import com.example.etlparser.entity.Patient;
import com.example.etlparser.service.in.ParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.javapoet.ClassName;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ParserServiceImpl implements ParserService {

    @Autowired
    private PatientDAO pDAO;
    @Autowired
    private HospedalizationDAO oDAO;
    @Autowired
    private AnalysisDAO aDAO;

    private static final Logger LOGGER = Logger.getLogger(ClassName.class.getName() );

    @Override
    public void uploadPatientHistory(PatientDTO petient) {
        LOGGER.log(Level.FINE,"up-1 "+petient.getSerialNumber());
        Patient paziente=petient.toEntity();
        if (paziente != null){
            LOGGER.log(Level.FINE,"up-2 "+paziente.getSerialNumber());
            if (paziente.getSerialNumber()!= null&&paziente.getSerialNumber()>0){
                LOGGER.log(Level.FINE,"up-3 "+paziente.getSerialNumber());
                Optional<Patient> p=pDAO.findById(paziente.getSerialNumber());
                if (p.isPresent()){
                    LOGGER.log(Level.FINE,"up-4 "+paziente.getSerialNumber());
                    Patient paziente1=p.get();
                    if (!paziente1.getLista().equals(paziente.getLista())){
                        LOGGER.log(Level.FINE,"up-5 "+paziente.getSerialNumber());
                        for (Hospitalization o: paziente.getLista()){
                            LOGGER.log(Level.FINE,"up-6 "+paziente.getSerialNumber());
                            if (!paziente1.getLista().contains(o)){
                                LOGGER.log(Level.FINE,"Paziente presente, aggioranti i le ops");
                                aDAO.save(o.getAnalisi());
                                oDAO.save(o);
                                paziente1.getLista().add(o);
                            }
                            else{
                                LOGGER.log(Level.FINE,"Paziente presente, NON aggioranti i le ops");
                            }
                        }
                        pDAO.save(paziente1);
                        LOGGER.log(Level.FINE,"Paziente presente, aggioranti i dati");
                    }
                    else
                        LOGGER.log(Level.FINE,"Paziente presente, liste uguali niente da aggiugnere");
                }else{
                    LOGGER.log(Level.FINE,"up-7 "+paziente.getSerialNumber());
                    if (paziente.getLista()!=null){
                        LOGGER.log(Level.FINE,"up-8 "+paziente.getSerialNumber());
                        if (!paziente.getLista().isEmpty()){
                            LOGGER.log(Level.FINE,"up-9 "+paziente.getSerialNumber());
                            boolean flag=true;
                            for (int i=0; i<paziente.getLista().size()&&flag;i++){
                                LOGGER.log(Level.FINE,"up-10 "+paziente.getSerialNumber());
                                if(paziente.getLista().get(i)==null){
                                    flag=false;
                                    LOGGER.log(Level.FINE,"5.1");
                                }
                                else{
                                    LOGGER.log(Level.FINE,"up-11 "+paziente.getSerialNumber());
                                    if(paziente.getLista().get(i).getAnalisi()== null){
                                        LOGGER.log(Level.FINE,"5.2");
                                        flag = false;
                                    }
                                }
                            }
                            if (flag){
                                List<Hospitalization> l=paziente.getLista();
                                paziente.setLista(new ArrayList<>());
                                pDAO.save(paziente);
                                for (Hospitalization o:l){
                                    aDAO.save(o.getAnalisi());
                                    oDAO.save(o);
                                }
                                LOGGER.log(Level.FINE,"salvato");
                            }else LOGGER.log(Level.FINE,"5");
                        }else LOGGER.log(Level.FINE,"4");
                    }else LOGGER.log(Level.FINE,"3");
                }
            }else LOGGER.log(Level.FINE,"2");
        }
        else LOGGER.log(Level.FINE,"1");
    }
    
}
