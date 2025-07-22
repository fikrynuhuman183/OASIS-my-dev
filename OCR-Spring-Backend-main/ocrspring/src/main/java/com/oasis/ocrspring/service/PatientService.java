package com.oasis.ocrspring.service;

import com.oasis.ocrspring.dto.*;
import com.oasis.ocrspring.dto.subdto.Risk_factors;
import com.oasis.ocrspring.model.Patient;
import com.oasis.ocrspring.model.TeleconEntry;
import com.oasis.ocrspring.repository.PatientRepository;
import com.oasis.ocrspring.repository.TeleconEntriesRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PatientService {
    private final PatientRepository patientRepo;
    private final TeleconEntriesRepository teleconEntriesRepo;
    private final String consentFormUploadDir;


    @Autowired
    public PatientService(PatientRepository patientRepo,
                          TeleconEntriesRepository teleconEntriesRepo,
                          @Value("${consentFormUploadDir}") String consentFormUploadDir) {
        this.patientRepo = patientRepo;
        this.teleconEntriesRepo = teleconEntriesRepo;
        this.consentFormUploadDir = consentFormUploadDir;
    }




    public Optional<Patient> getPaitentByIdAndClinicianId(String id, String clinicianId){
        return   patientRepo.findByIdAndClinicianId(new ObjectId(id), new ObjectId(clinicianId));

    }
    public Patient findAndUpdate (String id, String clinicianId , UpdatePatientDto updatePatientDto){
        Optional<Patient> patient =patientRepo.findByIdAndClinicianId(new ObjectId(id), new ObjectId(clinicianId));
        if(patient.isPresent()){
            Patient currentPatient=patient.get();
            currentPatient.setPatientName(updatePatientDto.getPatient_name());
            currentPatient.setGender(updatePatientDto.getGender());

            OffsetDateTime offsetDateTime = OffsetDateTime.parse(updatePatientDto.getDob());
            LocalDate date = offsetDateTime.toLocalDate();
            currentPatient.setDob(date);

            currentPatient.setRiskFactors(updatePatientDto.getRisk_factors());
            currentPatient.setHistoDiagnosis(updatePatientDto.getHisto_diagnosis());
            currentPatient.setContactNo(updatePatientDto.getContact_no());
            currentPatient.setSystemicDisease(updatePatientDto.getSystemic_disease());
            currentPatient.setFamilyHistory(updatePatientDto.getFamily_history());
            currentPatient.setMedicalHistory(updatePatientDto.getMedical_history());
            currentPatient.setUpdatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
            return patientRepo.save(currentPatient);

        }else{
            return null;
        }


    }




public Patient findOne(String patientId, String clinicianId){
    return patientRepo.findByPatientIdAndClinicianId(patientId,new ObjectId(clinicianId)).orElse(null);
}



public  Patient findPatient(String id,String clinicianId){

        ObjectId patientObjectId = new ObjectId(id);
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        return patientRepo.findByIdAndClinicianId(patientObjectId, clinicianObjectId).orElse(null);
    }

    public ResponseEntity<?> addPatient(
            String id,
            ConsentRequestDto data
    ) {
        try {
            // Check if patient ID already exists
            Patient patient = findOne(data.getPatientId(), id);
            if (patient != null) {
                return ResponseEntity.status(401).body("Patient ID already exists");
            }

            // Build new Patient directly, no file-handling
            Patient newPatient = new Patient();
            newPatient.setPatientId(data.getPatientId());
            newPatient.setClinicianId(new ObjectId(id));
            newPatient.setPatientName(data.getPatientName());
            newPatient.setRiskFactors(data.getRiskFactors());
            LocalDateTime dt = LocalDateTime.parse(
                data.getDob(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            );
            newPatient.setDob(dt.toLocalDate());

            newPatient.setGender(data.getGender());
            newPatient.setHistoDiagnosis(data.getHistoDiagnosis());
            newPatient.setMedicalHistory(data.getMedicalHistory());
            newPatient.setFamilyHistory(data.getFamilyHistory());
            newPatient.setSystemicDisease(data.getSystemicDisease());
            newPatient.setContactNo(data.getContactNo());
            // You can store the consentForm path or leave it empty
            newPatient.setConsentForm("");
            // Timestamps
            LocalDateTime now = LocalDateTime.now();
            newPatient.setCreatedAt(now);
            newPatient.setUpdatedAt(now);

            // Save patient
            patientRepo.save(newPatient);
            return ResponseEntity.status(200).body(new ConsentResponseDto(newPatient));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponseDto("Internal Server Error", e.toString()));
        }
    }

    private ResponseEntity<?> getResponse(ConsentRequestDto data, MultipartFile files, String fileName, List<String> uploadedURIs) {
        try {
            // Create the directory path first
            Path dirPath = Paths.get(consentFormUploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            // Create the full file path
            Path filePath = dirPath.resolve(fileName);
            Files.copy(files.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String fileDownUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/files/")
                    .path(fileName)
                    .toUriString();
            uploadedURIs.add(fileDownUri);

            Patient newPatient = new Patient();

            newPatient.setPatientId(data.getPatientId());
            newPatient.setClinicianId(new ObjectId(data.getClinicianId()));
            newPatient.setPatientName(data.getPatientName());
            newPatient.setRiskFactors(data.getRiskFactors());
            newPatient.setDob(LocalDate.parse(data.getDob(), DateTimeFormatter.ISO_LOCAL_DATE));
            newPatient.setGender(data.getGender());
            newPatient.setHistoDiagnosis(data.getHistoDiagnosis());
            newPatient.setMedicalHistory(data.getMedicalHistory());
            newPatient.setFamilyHistory(data.getFamilyHistory());
            newPatient.setSystemicDisease(data.getSystemicDisease());
            newPatient.setContactNo(data.getContactNo());
            newPatient.setConsentForm(data.getPatientId().toString()+".pdf");
            newPatient.setCreatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
            newPatient.setUpdatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));


            patientRepo.save(newPatient);
            return ResponseEntity.status(200).body(new ConsentResponseDto(newPatient));
        } catch (MultipartException ex) {
            return ResponseEntity.status(500).body(new ErrorResponseDto("Internal " +
                    "Server Error!", ex.toString()));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponseDto("Internal " +
                    "Server Error!", e.toString()));
        }
    }

    public List<SearchPatientDto> searchPatients(String clinicianId, String searchQuery, int pageQuery, int pageSize, Sort sort) {
        Pageable pageable = PageRequest.of(pageQuery - 1, pageSize, sort);
        List<Patient> patientList = patientRepo.findByClinicianIdAndSearch(new ObjectId(clinicianId), searchQuery, pageable);
        List<SearchPatientDto> searchRes = new ArrayList<>();
        for (Patient patient : patientList) {
            searchRes.add(new SearchPatientDto(patient.getId().toString(), patient.getPatientId(), patient.getPatientName(),patient.getDob().toString(), patient.getGender()));
        }
        return searchRes;
    }

    public List<SearchPatientDto> getAllPatients(String clinicianId,int pageQuery,int pageSize,Sort sort){
        Pageable pageable = PageRequest.of(pageQuery-1,pageSize,sort);
        List<Patient>  patientList = patientRepo.findByClinicianId(new ObjectId(clinicianId),pageable);
        List<SearchPatientDto> searchRes = new ArrayList<>();
        for(Patient patient:patientList){
            searchRes.add(new SearchPatientDto(patient.getId().toString(),patient.getPatientId(),patient.getPatientName(),patient.getDob().toString(),patient.getGender()));
        }
        return searchRes;
    }
    public Patient getPatientByPatientIDAndClinicianId(String patientId, String clinicianId){
        return patientRepo.findByPatientIdAndClinicianId(patientId,new ObjectId(clinicianId)).orElse(null);

    }

    public Patient getSharedPatient(String patientId, String reviewerId) {
        Optional<TeleconEntry> entry = teleconEntriesRepo.findByPatientAndReviewersIn(new ObjectId(patientId), new ObjectId(reviewerId));
        Optional<Patient> patient = patientRepo.findById(patientId);

        if (entry.isPresent() && patient.isPresent()) {
            return patient.get();
        } else {
            return null;
        }
    }



    public Map<String, Double> calculateRiskHabitPercentages() {
        List<Patient> patients = patientRepo.findAll();
        long totalPatients = patients.size();
        Map<String, Integer> habitCounts = new HashMap<>();

        patients.forEach(patient -> {
            List<Risk_factors> riskFactors = patient.getRiskFactors();
            if (riskFactors != null)  // Add this null check
                riskFactors.forEach(riskFactor ->
                    habitCounts.put(riskFactor.getHabit(),
                            habitCounts.getOrDefault(riskFactor.getHabit(), 0) + 1
                    )
                );

        });

        Map<String, Double> percentages = new HashMap<>();
        habitCounts.forEach((habit, count) ->
            percentages.put(habit, (count * 100.0) / totalPatients));

        return percentages;
    }

    public long countPatients() {
        return patientRepo.count();
    }

    public Patient createPatient(CreatePatientDto createPatientDto, String clinicianId) {
        try {
            // Generate unique patient ID
            String patientId = generateUniquePatientId();
            
            // Check if patient ID already exists
            Patient existingPatient = findOne(patientId, clinicianId);
            if (existingPatient != null) {
                throw new RuntimeException("Generated patient ID already exists");
            }
            
            Patient newPatient = new Patient();
            
            // Set basic information
            newPatient.setPatientId(patientId);
            newPatient.setClinicianId(new ObjectId(clinicianId));
            newPatient.setPatientName(createPatientDto.getPatientName());
            newPatient.setGender(createPatientDto.getGender());
            newPatient.setContactNo(createPatientDto.getContactNo());
            
            // Parse and set date of birth
            if (createPatientDto.getDob() != null && !createPatientDto.getDob().isEmpty()) {
                try {
                    // Handle ISO date string from Flutter
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(createPatientDto.getDob());
                    LocalDate date = offsetDateTime.toLocalDate();
                    newPatient.setDob(date);
                } catch (Exception e) {
                    // Fallback: try parsing as LocalDate
                    try {
                        LocalDate date = LocalDate.parse(createPatientDto.getDob());
                        newPatient.setDob(date);
                    } catch (Exception ex) {
                        throw new RuntimeException("Invalid date format: " + createPatientDto.getDob());
                    }
                }
            }
            
            // Convert risk factors from strings to Risk_factors objects
            if (createPatientDto.getRiskFactors() != null && !createPatientDto.getRiskFactors().isEmpty()) {
                List<Risk_factors> riskFactorsList = new ArrayList<>();
                for (String riskFactor : createPatientDto.getRiskFactors()) {
                    Risk_factors rf = new Risk_factors();
                    rf.setHabit(riskFactor);
                    rf.setDuration(""); // Default empty, can be updated later
                    rf.setFrequency(""); // Default empty, can be updated later
                    riskFactorsList.add(rf);
                }
                newPatient.setRiskFactors(riskFactorsList);
            }
            
            // Set medical history
            if (createPatientDto.getMedicalHistory() != null && !createPatientDto.getMedicalHistory().isEmpty()) {
                newPatient.setMedicalHistory(Arrays.asList(createPatientDto.getMedicalHistory()));
            }
            
            // Set family history
            if (createPatientDto.getFamilyHistory() != null && !createPatientDto.getFamilyHistory().isEmpty()) {
                newPatient.setFamilyHistory(Arrays.asList(createPatientDto.getFamilyHistory()));
            }
            
            // Set systemic diseases - convert list to comma-separated string
            if (createPatientDto.getSystemicDisease() != null && !createPatientDto.getSystemicDisease().isEmpty()) {
                String systemicDiseaseStr = String.join(", ", createPatientDto.getSystemicDisease());
                newPatient.setSystemicDisease(systemicDiseaseStr);
            }
            
            // Set timestamps
            LocalDateTime now = LocalDateTime.now();
            newPatient.setCreatedAt(now);
            newPatient.setUpdatedAt(now);
            
            // Set default values
            newPatient.setHistoDiagnosis(""); // Empty initially
            newPatient.setConsentForm(""); // Will be set when consent form is uploaded separately
            
            // Save and return
            return patientRepo.save(newPatient);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create patient: " + e.getMessage(), e);
        }
    }

    private String generateUniquePatientId() {
        // Generate a unique patient ID
        String prefix = "PAT";
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(1000);
        return prefix + timestamp + String.format("%03d", random);
    }

    
}

