package com.oasis.ocrspring.controller;


import com.oasis.ocrspring.dto.PatientDetailsResDto;
import com.oasis.ocrspring.dto.SearchPatientDto;
import com.oasis.ocrspring.dto.SharedResponseDto;
import com.oasis.ocrspring.dto.UpdatePatientDto;
import com.oasis.ocrspring.model.Patient;
import com.oasis.ocrspring.model.User;
import com.oasis.ocrspring.service.PatientService;
import com.oasis.ocrspring.service.ResponseMessages.ErrorMessage;
import com.oasis.ocrspring.service.ReviewService;
import com.oasis.ocrspring.service.ReviewerResDto;
import com.oasis.ocrspring.service.auth.AuthenticationToken;
import com.oasis.ocrspring.service.auth.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/api/user/patient/")
public class PatientController {


    private final PatientService patientService;


    private final AuthenticationToken authenticationToken;

    private final TokenService tokenService;

    private final ReviewService reviewerService;

    @Autowired
    public PatientController(
            PatientService patientService,
            AuthenticationToken authenticationToken,
            TokenService tokenService,
            ReviewService reviewerService) {
        this.patientService = patientService;
        this.authenticationToken = authenticationToken;
        this.tokenService = tokenService;
        this.reviewerService = reviewerService;
    }

    @ApiIgnore
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/swagger-ui.html");
    }

    static String internalServerError="Internal Server Error";
    static String unAuthorized="Unauthorized Access";
    @PostMapping("update/{id}")
    public ResponseEntity<?> updatePatient(HttpServletRequest request, HttpServletResponse response, @PathVariable String id, @RequestBody UpdatePatientDto updatePatient)throws IOException {

        authenticationToken.authenticateRequest(request, response);


        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }
        String clinicianId=request.getAttribute("_id").toString();
        Optional<Patient> patient = patientService.getPaitentByIdAndClinicianId(id, clinicianId);
        if(patient.isEmpty()){
            return ResponseEntity.status(401).body(new ErrorMessage("Patient ID does not exist"));
        }

        Patient updatedPatent =patientService.findAndUpdate(id,clinicianId,updatePatient);
        if(updatedPatent==null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(unAuthorized));
        }

        Map<String, Object> finalRes = new HashMap<>(updatedPatent.toMap()); // Assuming you have a method to convert Patient to Map
        finalRes.put("message", "Successfully added");

        return ResponseEntity.ok(finalRes);

    }



    //get all patients
    @GetMapping("/get")
    public ResponseEntity<?> getPatient(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false)String filter
    ) throws IOException {
        authenticationToken.authenticateRequest(request, response);

        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        String clinicianId=request.getAttribute("_id").toString();

        int pageSize = 20;
        int pageQuery = page == null ? 1 : Integer.parseInt(page);
        String searchQuery = search == null ? "" : search;
        Sort.Direction sortDirection = sort == null || sort.equals("false") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort.Order sortOrder;
        switch (filter != null ? filter : "") {
            case "Name":
                sortOrder = Sort.Order.by("patientName").with(sortDirection);
                break;
            case "Age":
                sortOrder = Sort.Order.by("DOB").with(sortDirection);
                break;
            case "Gender":
                sortOrder = Sort.Order.by("gender").with(sortDirection);
                break;
            case "Created Date":
                sortOrder = Sort.Order.by("createdAt").with(sortDirection);
                break;
            case "Updated Date":
                sortOrder = Sort.Order.by("updatedAt").with(sortDirection);
                break;
            default:
                sortOrder = Sort.Order.by("patientId").with(sortDirection);
                break;
        }
        
        List<SearchPatientDto> patients;

        try{
            if(searchQuery.isEmpty()){
                patients = patientService.getAllPatients(clinicianId,pageQuery,pageSize,Sort.by(sortOrder));
            }else{
                patients = patientService.searchPatients(clinicianId,searchQuery,pageQuery,pageSize,Sort.by(sortOrder));

            }
            Map<String, Object> finalRes = new HashMap<>(); // Assuming you have a method to convert Patient to Map
            finalRes.put("patients", patients);
            return ResponseEntity.ok(finalRes);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(internalServerError));
        }



    }


    //check if a patient exists
    @GetMapping("/check/{id}")
    public ResponseEntity<?> checkPatient(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws IOException {
        authenticationToken.authenticateRequest(request, response);

        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }
        try {
            Patient patient = patientService.getPatientByPatientIDAndClinicianId(id, request.getAttribute("_id").toString());
            if (patient != null) {
                return ResponseEntity.status(200).body(Collections.singletonMap("exists", true));
            } else {
                return ResponseEntity.status(200).body(Collections.singletonMap("exists", false));
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(internalServerError));
        }
    }

    //get one id
    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws IOException {
        authenticationToken.authenticateRequest(request, response);

        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }
        try{
            Optional<Patient> patientOptional =patientService.getPaitentByIdAndClinicianId(id,request.getAttribute("_id").toString());
            if(patientOptional.isEmpty()){
                return ResponseEntity.status(404).body(new ErrorMessage("Patient not found"));
            }
            Patient patient = patientOptional.get();
            return ResponseEntity.ok( new PatientDetailsResDto(
                    patient.getSystemicDisease(),
                    patient.getId().toString(),
                    patient.getPatientId(),
                    patient.getClinicianId().toString(),
                    patient.getDob().toString(),
                    patient.getPatientName(),
                    patient.getRiskFactors(),
                    patient.getGender(),
                    patient.getHistoDiagnosis(),
                    patient.getMedicalHistory(),
                    patient.getFamilyHistory(),
                    patient.getContactNo(),
                    patient.getConsentForm(),
                    patient.getCreatedAt(),
                    patient.getUpdatedAt()
            ));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(internalServerError));
        }
    }

    //get one shared id
    //id is patient id
    @GetMapping("/shared/{id}")
    public ResponseEntity<?> getSharedPatient(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws IOException {
        authenticationToken.authenticateRequest(request, response);

        if (!tokenService.checkPermissions(request, Collections.singletonList("200"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            String reviewerId = request.getAttribute("_id").toString();
            Patient patient = patientService.getSharedPatient(id, reviewerId);

            if (patient != null) {
                SharedResponseDto details =new  SharedResponseDto(patient);
                return ResponseEntity.ok(details);

            } else {
                return ResponseEntity.status(404).body(new ErrorMessage("Patient not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(internalServerError));
        }
    }

    @GetMapping("reviewer/all")
    public ResponseEntity<?> getAllReviewers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationToken.authenticateRequest(request, response);

        if (!tokenService.checkPermissions(request, Arrays.asList("300", "200"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }
        try {
            List<User> reviewers = reviewerService.getAllReviewers();
            List<ReviewerResDto> reviewerResDtos = new ArrayList<>();
            for (User reviewer : reviewers) {
                reviewerResDtos.add(new ReviewerResDto(reviewer));
            }
            if (reviewers != null && !reviewers.isEmpty()) {
                return ResponseEntity.ok(reviewerResDtos);
            } else {
                return ResponseEntity.status(404).body(new ErrorMessage("Reviewers Not Found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage("Internal Server Error!"));
        }
    }
}
