package com.oasis.ocrspring.controller;

import com.oasis.ocrspring.dto.PatientTeleconRequest;
import com.oasis.ocrspring.dto.ReviewRequestDto;
import com.oasis.ocrspring.service.AssignmentService;
import com.oasis.ocrspring.service.ResponseMessages.ErrorMessage;
import com.oasis.ocrspring.service.TeleconEntriesService;
import com.oasis.ocrspring.service.auth.AuthenticationToken;
import com.oasis.ocrspring.service.auth.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/entry")
public class EntryController {
    private final TeleconEntriesService teleconService;
    private final AuthenticationToken authenticationToken;
    private final TokenService tokenService;
    private final AssignmentService assignmentService;
    @Autowired
    public EntryController(TeleconEntriesService teleconService,
                           AuthenticationToken authenticationToken,
                           TokenService tokenService,
                           AssignmentService assignmentService) {
        this.teleconService = teleconService;
        this.authenticationToken = authenticationToken;
        this.tokenService = tokenService;
        this.assignmentService = assignmentService;
    }
    public static final String REVIEWER_ID = "reviewer_id";
    static final String UNAUTHORIZED_ACCESS = "Unauthorized Access";

    // connect entry to the service layer
    @ApiIgnore
    @RequestMapping(value = "/")
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/swagger-ui.html");
    }

    //add a teleconsultation entry
    @PostMapping("/add/{id}")
    public ResponseEntity<?> addTeleconsultationEntry(HttpServletRequest request, HttpServletResponse response,
                                                      @PathVariable String id,
                                                      @RequestBody PatientTeleconRequest newPatient)
    throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        // add a teleconsultation entry
        String clinicianId=request.getAttribute("_id").toString();
        return teleconService.patientTeleconEntry(id, clinicianId, newPatient);
    }

    //get all entries for the cliniciaan
    @GetMapping("/get")
    public ResponseEntity<?> getAllEntries(HttpServletRequest request, HttpServletResponse response,
                                           @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
                                           @RequestParam(name = "filter", required = false, defaultValue = "Created Date") String filter)
    throws IOException{ //id is clinician Id
        // get all teleconsultation entries
        int pageSize = 20;
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        return teleconService.getAllUserEntries(clinicianId, page, filter, pageSize);
    }

    //get patient entries
    @GetMapping("/get/patient/{id}")
    public ResponseEntity<?> getPatientEntries(HttpServletRequest request, HttpServletResponse response,
                                               @PathVariable String id,
                                               @RequestParam(name = "page",required = false,defaultValue ="1") Integer page,
                                               @RequestParam(name = "filter",required = false,defaultValue = "Created Date") String filter)
    throws IOException{
        int pageSize = 20;
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        // get patient entries
        String clinicianId = request.getAttribute("_id").toString();
        return teleconService.getUserEntryById(clinicianId,id,page,filter,pageSize);
    }

    //get shared patient entries (view only data)
    //id is the patient id
    @GetMapping("/shared/patient/{id}")
    public ResponseEntity<?> getSharedPatientEntries(HttpServletRequest request, HttpServletResponse response,
                                                     @PathVariable String id,
                                                     @RequestParam(name = "page",required = false, defaultValue = "1") Integer page,
                                                     @RequestParam(name = "filter",required = false,defaultValue = "Created Date") String filter)
    throws IOException{
        // get shared patient entries
        int pageSize = 20;
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("200"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        return teleconService.getSharedPatient(clinicianId,id,filter,pageSize,page);
    }

    // get one entry details added by users
    // id is entry _id
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getEntry(HttpServletRequest request, HttpServletResponse response,
                           @PathVariable String id) throws IOException{

        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("200"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        // get one entry
        return teleconService.getEntryDetails(clinicianId,id);
    }

    //get new review count
    @GetMapping("/count/newreviews")
    public ResponseEntity<?> countNewReviews(HttpServletRequest request, HttpServletResponse response)
    throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("200"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        // get new review count
        return teleconService.countNewReviews(clinicianId);
    }

    //get unreviewed entry count
    @GetMapping("/count/newentries")
    public ResponseEntity<?> getUnreviewedEntryCount(HttpServletRequest request, HttpServletResponse response)
    throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("200"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        // get unreviewed entry count
        return assignmentService.getUnreviewedEntryCount(clinicianId);
    }

    //add a reviewer by user
    //id is entry _id
    @PostMapping("/reviewer/add/{id}")
    public ResponseEntity<?> addReviewer(HttpServletRequest request, HttpServletResponse response,
                                         @PathVariable String id,
                                         @RequestBody Map<String,String> payload
                              ) throws IOException{
        String reviewerId = payload.get(REVIEWER_ID);
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        // add a reviewer
        return teleconService.addReviewer(clinicianId,id,reviewerId);
    }

    //remove a reviewer by user
    //id is entry _id
    @PostMapping("/reviewer/remove/{id}")
    public ResponseEntity<?> removeReviewer(HttpServletRequest request, HttpServletResponse response,
                                 @PathVariable String id,
                                 @RequestBody Map<String,String> payload) throws IOException{
        // remove a reviewer
        String reviewerId = payload.get(REVIEWER_ID);
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("200"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        // add a reviewer
        return teleconService.removeReviewer(clinicianId,id,reviewerId);
    }

    //delete an entry by user
    //id is entry _id
    @PostMapping("/delete/{id}")
    public ResponseEntity<?> deleteEntry(HttpServletRequest request, HttpServletResponse response,
                              @PathVariable String id) throws IOException{

        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        // delete an entry
        return teleconService.deleteEntry(clinicianId,id);
    }

    //get all shared entries
    @GetMapping("/shared/all")
    public ResponseEntity<?> getAllSharedEntries(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam(name = "page",required = false, defaultValue = "1") Integer page,
                                      @RequestParam(name = "filter",required = false) String filter) throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("200"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        // get all shared entries
        final int pageSize = 20;
        return teleconService.getAllSharedEntries(page,pageSize,clinicianId,filter);
    }

    //get one shared entry(view only)
    //id is entry _id
    @GetMapping("/shared/{id}")
    public ResponseEntity<?> getSharedEntry(HttpServletRequest request, HttpServletResponse response,
                                            @PathVariable String id
                                            ) throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("200"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        // get one shared entry
        return teleconService.getSharedEntry(id,clinicianId);
    }

    //get assigned entry details
    //id is assignment _id
    @GetMapping("/shared/data/{id}")
    public ResponseEntity<?> getAssignedEntryDetails(HttpServletRequest request, HttpServletResponse response,
                                                     @PathVariable String id) throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("200"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        // get assigned entry details
        return teleconService.getAssignedEntryDetails(id);
    }

    //get entry reviews
    //id is entry _id
    @GetMapping("/reviews/{id}")
    public ResponseEntity<?> getEntryReviews(HttpServletRequest request, HttpServletResponse response,
                                             @PathVariable String id) throws IOException {
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, List.of("200","300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        // get entry reviews
        return teleconService.getEntryReviews(id);
    }

    //change a reviewer(reviewer assignes another)
    @PostMapping("/reviewer/change/{id}")
    public ResponseEntity<?> changeReviewer(HttpServletRequest request, HttpServletResponse response,
                                            @PathVariable String id,
                                            @RequestBody Map<String,String> payload) throws IOException{


        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("200"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        String reviewerId = payload.get(REVIEWER_ID);
        // change a reviewer
        return teleconService.changeReviewer(id,clinicianId,reviewerId);
    }

    //add new review
    //id is telecon_id
    @PostMapping("/review/{id}")
    public ResponseEntity<?> addNewReview(HttpServletRequest request, HttpServletResponse response,
                                            @PathVariable String id,
                                          @RequestBody ReviewRequestDto reviewDetails) throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("200"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        // add new review
        return teleconService.addReview(clinicianId, id, reviewDetails);
    }

    //mark as read
    //id is assignment _id
    @PostMapping("/mark/{id}")
    public ResponseEntity<?> markAsRead(HttpServletRequest request, HttpServletResponse response,
                                        @PathVariable String id) throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("200"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        // mark as read
        return teleconService.markAsRead(id );
    }

    //mark as read
    //id is entry _id
    @PostMapping("/open/{id}")
    public ResponseEntity<?> markAsOpen(HttpServletRequest request, HttpServletResponse response,
                                        @PathVariable String id) throws IOException{
        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new ErrorMessage(UNAUTHORIZED_ACCESS));
        }
        // mark as open
        return teleconService.markAsOpen(id );
    }
}

