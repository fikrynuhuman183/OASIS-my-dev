package com.oasis.ocrspring.controller;

import com.oasis.ocrspring.dto.*;
import com.oasis.ocrspring.model.Patient;
import com.oasis.ocrspring.service.ImageService;
import com.oasis.ocrspring.service.PatientService;
import com.oasis.ocrspring.service.ReportService;
import com.oasis.ocrspring.service.ResponseMessages.ErrorMessage;
import com.oasis.ocrspring.service.auth.AuthenticationToken;
import com.oasis.ocrspring.service.auth.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v3/user/upload")
public class UploadController {
    private final ImageService imageService;
    private final ReportService reportServ;
    private final PatientService patientService;
    private final AuthenticationToken authenticationToken;
    private final TokenService tokenService;
    static final String UNAUTHORIZED_ACCESS = "Unauthorized Access";

    @Autowired
    public UploadController(ImageService imageService,
                            ReportService reportServ,
                            PatientService patientService,
                            AuthenticationToken authenticationToken,
                            TokenService tokenService) {
        this.imageService = imageService;
        this.reportServ = reportServ;
        this.patientService = patientService;
        this.authenticationToken = authenticationToken;
        this.tokenService = tokenService;
    }

    @PostMapping(value = "/images/{id}")
    public ResponseEntity<UploadImageResponse> uploadImages(
            HttpServletRequest request, HttpServletResponse response,
            @PathVariable String id,
            @RequestPart("data") ImageRequestDto data,
            @RequestPart("files") List<MultipartFile> files) throws IOException {

        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new UploadImageResponse(null,UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        return imageService.uploadImages(data, id,clinicianId, files);
    }

    @PostMapping(value = "/reports/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadReportResponse> uploadReports(HttpServletRequest request, HttpServletResponse response,
                                                              @PathVariable String id,
                                                              @RequestPart("data") ReportsRequestDto data,
                                                              @RequestPart("files") List<MultipartFile> files) throws IOException{

        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new UploadReportResponse(null,UNAUTHORIZED_ACCESS));
        }
        String clinicianId=request.getAttribute("_id").toString();
        return reportServ.uploadReports(data, id,clinicianId, files);
    }

    @PostMapping("/patient")
    public ResponseEntity<?> addConsentForm(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestPart("data") ConsentRequestDto data
    ) throws IOException {
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }

        // Print out the user attributes for debugging
        System.out.println("=== [Debug] request attributes: ===");
        System.out.println("=== _id (clinician's user ID) === " + request.getAttribute("_id"));
        // If you have a “user” object placed in request attributes:
        System.out.println("=== user object === " + request.getAttribute("user"));

        // The “_id” attribute should be your clinician's user ID in MongoDB
        String clinicianId = request.getAttribute("_id").toString();

        // Then pass that to your PatientService
        return patientService.addPatient(clinicianId, data);
    }
}