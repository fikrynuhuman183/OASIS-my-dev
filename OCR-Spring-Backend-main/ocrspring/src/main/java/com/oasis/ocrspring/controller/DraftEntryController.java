package com.oasis.ocrspring.controller;

import com.oasis.ocrspring.dto.PatientTeleconRequest;
import com.oasis.ocrspring.service.ResponseMessages.ErrorMessage;
import com.oasis.ocrspring.service.auth.AuthenticationToken;
import com.oasis.ocrspring.service.auth.TokenService;
import com.oasis.ocrspring.service.draftServices.DraftEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@RestController
@RequestMapping("/api/user/draftentry")
public class DraftEntryController {
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
    private final DraftEntryService draftEntryService;
    private final TokenService tokenService;
    private final AuthenticationToken authenticationToken;

    @Autowired
    public DraftEntryController(DraftEntryService draftEntryService,
                                TokenService tokenService,AuthenticationToken authenticationToken)
    {
        this.draftEntryService = draftEntryService;
        this.tokenService = tokenService;
        this.authenticationToken = authenticationToken;
    }

    @ApiIgnore
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/swagger-ui.html");
    }

    @PostMapping("/add/{id}")
    public ResponseEntity<?> addDraftTeleconEntry(HttpServletRequest request, HttpServletResponse response,
                                                  @PathVariable String id,
                                                  @RequestBody PatientTeleconRequest patientRequest)
            throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        return draftEntryService.addDraftTeleconEntry( id, clinicianId,patientRequest);
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAllDraftEntries(HttpServletRequest request, HttpServletResponse response,
                                                @RequestParam(name = "_page", required = false, defaultValue = "1") Integer page,
                                                @RequestParam(name = "_query", required = false, defaultValue = "Created Date") String filter)
            throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        int pageSize = 20;
        return draftEntryService.getAllDraftEntries(page,filter,clinicianId,pageSize);
    }

    @GetMapping("/get/patient/{id}")
    public ResponseEntity<?> getPtientEntries(HttpServletRequest request, HttpServletResponse response,
                                   @PathVariable String id,
                                   @RequestParam(name = "_page", required = false, defaultValue = "1") Integer page,
                                   @RequestParam(name = "_query", required = false, defaultValue = "Created Date") String filter)
    throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        int pageSize = 20;
        return draftEntryService.getPatientDraftEntries(page,pageSize,clinicianId,filter,id);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getEntryDetails(HttpServletRequest request, HttpServletResponse response,
                                             @PathVariable String id) throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        return draftEntryService.getEntryDetails(clinicianId,id);
    }
}