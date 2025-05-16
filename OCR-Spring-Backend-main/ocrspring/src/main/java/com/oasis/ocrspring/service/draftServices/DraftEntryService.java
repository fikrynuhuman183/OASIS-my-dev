package com.oasis.ocrspring.service.draftServices;

import com.oasis.ocrspring.dto.*;
import com.oasis.ocrspring.model.TeleconEntry;
import com.oasis.ocrspring.model.draftModels.DraftEntry;
import com.oasis.ocrspring.model.Image;
import com.oasis.ocrspring.model.Patient;
import com.oasis.ocrspring.model.Report;
import com.oasis.ocrspring.repository.ImageRepository;
import com.oasis.ocrspring.repository.PatientRepository;
import com.oasis.ocrspring.repository.ReportRepository;
import com.oasis.ocrspring.repository.TeleconEntriesRepository;
import com.oasis.ocrspring.repository.draftRepos.DraftEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DraftEntryService {
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error!";
    private final DraftEntryRepository draftEntryRepo;
    private final PatientRepository patientRepo;
    private final ImageRepository imageRepo;
    private final ReportRepository reportRepo;
    private final TeleconEntriesRepository teleconEntryRepo;


    @Autowired
    public DraftEntryService(DraftEntryRepository draftEntryRepo,PatientRepository patientRepo
                            ,ImageRepository imageRepo, ReportRepository reportRepo,
                             TeleconEntriesRepository teleconEntryRepo){
        this.draftEntryRepo = draftEntryRepo;
        this.patientRepo = patientRepo;
        this.imageRepo = imageRepo;
        this.reportRepo = reportRepo;
        this.teleconEntryRepo = teleconEntryRepo;
    }

    public List<DraftEntry> allDraftEntryDetails() {
        return draftEntryRepo.findAll();
    }

    public ResponseEntity<?> addDraftTeleconEntry(String id, String clinicianId, PatientTeleconRequest patientRequest){
        ObjectId patientId = new ObjectId(id);
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        try {
            Patient patient = patientRepo.findByIdAndClinicianId(patientId, clinicianObjectId).orElse(null);
            if (patient != null) {
                DraftEntry newDraftEntry = new DraftEntry();
                newDraftEntry.setClinicianId(clinicianObjectId);
                newDraftEntry.setPatient(patient.getClinicianId());
                newDraftEntry.setStartTime(OffsetDateTime.parse(patientRequest.getStartTime()).toLocalDateTime());
                newDraftEntry.setEndTime(OffsetDateTime.parse(patientRequest.getEndTime()).toLocalDateTime());
                newDraftEntry.setComplaint(patientRequest.getComplaint());
                newDraftEntry.setFindings(patientRequest.getFindings());
                newDraftEntry.setCurrentHabits(patientRequest.getCurrentHabits());
                ResponseEntity<MessageDto> response = saveDraftEntry(newDraftEntry);
                if (response != null) return response;
                return ResponseEntity.status(200).body(newDraftEntry);
            }else{
                return ResponseEntity.status(404).body(new MessageDto("Patient is not registered"));
            }
        }catch(Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }
    }

    private ResponseEntity<MessageDto> saveDraftEntry(DraftEntry newDraftEntry) {
        try {
            draftEntryRepo.save(newDraftEntry);
        }catch(Exception e){
            return ResponseEntity.status(500).body(new MessageDto("Draft Tele consultation entry failed"));
        }
        return null;
    }

    public ResponseEntity<?> getAllDraftEntries(int page,String filter,String clinicianId,int pageSize){
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        Pageable pageable = PageRequest.of(page-1,pageSize, Sort.by(Sort.Direction.DESC,getSortField(filter)));
        try {
            Page<DraftEntry> draftEntry = draftEntryRepo.findByClinicianId(clinicianObjectId, pageable);
            List<DraftEntryResponseDto> response = new ArrayList<>();

            if (!draftEntry.isEmpty()) {
                List<DraftEntry> draftEntryList = draftEntry.getContent();
                for (DraftEntry newEntry : draftEntryList) {
                    Patient patient = patientRepo.findById(newEntry.getPatient()).orElse(null);
                    if(patient != null){
                        PatientDetailsDto patientDetails = new PatientDetailsDto(patient);
                        DraftEntryResponseDto draftEntryResponse = new DraftEntryResponseDto(newEntry, patientDetails);
                        response.add(draftEntryResponse);
                    }

                }

            }
            return ResponseEntity.status(200).body(response);
        }catch (Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }
    }

    public ResponseEntity<?> getPatientDraftEntries(int page,int pageSize,String clinicianId,String filter, String id){
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        ObjectId patientObjectId = new ObjectId(id);
        Pageable pageable = PageRequest.of(page-1,pageSize,Sort.by(Sort.Direction.DESC,getSortField(filter)));
        try{
            Page<TeleconEntry> teleconEntry = teleconEntryRepo.findByPatientAndClinicianId(patientObjectId,clinicianObjectId,pageable);
            List<PopulatedTeleconsultationEntry> response = new ArrayList<>();

            if (!teleconEntry.isEmpty()) {
                List<TeleconEntry> teleconEntryList = teleconEntry.getContent();
                for (TeleconEntry newEntry : teleconEntryList) {
                    Patient patient = patientRepo.findById(newEntry.getPatient()).orElse(null);
                    if(patient != null){
                        PatientDetailsDto patientDetails = new PatientDetailsDto(patient);
                        PopulatedTeleconsultationEntry draftEntryResponse = new PopulatedTeleconsultationEntry(newEntry, patientDetails);
                        response.add(draftEntryResponse);
                    }

                }
                return ResponseEntity.status(200).body(response);
            }else {
                return ResponseEntity.status(500).body(new MessageDto("Entry Not Found"));
            }

        }catch (Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }
    }

    public ResponseEntity<?> getEntryDetails(String clinicianId,String id){
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        ObjectId draftEntryId = new ObjectId(id);

        try{
            DraftEntry draftEntry = draftEntryRepo.findByClinicianIdAndId(clinicianObjectId,draftEntryId).orElse(null);
            if (draftEntry != null){
                Patient patient = patientRepo.findById(draftEntry.getPatient()).orElse(null);
                PatientDetailsDto patientDetails = new PatientDetailsDto(patient);
                List<ObjectId> imageIdList = draftEntry.getImages();
                List<ObjectId> reportIdList = draftEntry.getReports();
                List<Image> imageList = new ArrayList<>();
                List<Report> reportList = new ArrayList<>();

                for(ObjectId imageId: imageIdList){
                    Image image = imageRepo.findById(imageId);
                    imageList.add(image);
                }

                for(ObjectId reportId: reportIdList){
                    Report report = reportRepo.findById(reportId);
                    reportList.add(report);
                }
                PopulatedDraftEntryDto result = new PopulatedDraftEntryDto(draftEntry,patientDetails,imageList,reportList);
                return ResponseEntity.status(200).body(result);

            }else {
                return ResponseEntity.status(404).body(new MessageDto("Draft Entry not found"));
            }
        }catch (Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }
    }

    private String getSortField(String filter){
        if(filter.equals("Updated At")){
            return "updatedAt";
        }
        return "createdAt";
    }
}
