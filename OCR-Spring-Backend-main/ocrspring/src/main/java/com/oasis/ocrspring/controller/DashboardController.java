package com.oasis.ocrspring.controller;

import com.oasis.ocrspring.dto.UploadDraftReportResponse;
import com.oasis.ocrspring.service.ImageService;
import com.oasis.ocrspring.service.PatientService;
import com.oasis.ocrspring.service.UserService;
import com.oasis.ocrspring.service.auth.AuthenticationToken;
import com.oasis.ocrspring.service.auth.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.oasis.ocrspring.controller.UploadController.UNAUTHORIZED_ACCESS;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {


    private final AuthenticationToken authenticationToken;
    private final TokenService tokenService;
    private final PatientService patientService;
    private final UserService userService;
    private final ImageService imageService;

    @Autowired
    public DashboardController(AuthenticationToken authenticationToken, TokenService tokenService, PatientService patientService, UserService userService, ImageService imageService) {
        this.authenticationToken = authenticationToken;
        this.tokenService = tokenService;
        this.patientService = patientService;
        this.userService = userService;
        this.imageService = imageService;
    }


    @ApiIgnore
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/swagger-ui.html");
    }

    @GetMapping("/percentages")
    public ResponseEntity<?> riskHabitPercentage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationToken.authenticateRequest(request, response);

        if(!tokenService.checkPermissions(request, Collections.singletonList("110"))){
            return ResponseEntity.status(401).body(new UploadDraftReportResponse(null,UNAUTHORIZED_ACCESS));
        }

        try {
            Map<String, Double> percentages = patientService.calculateRiskHabitPercentages();
            List<String> formattedList = percentages.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue() + "%")
                    .toList();

            return ResponseEntity.ok(formattedList);
        }catch(Exception e){
            return ResponseEntity.status(500).body(e);
        }
    }
    @GetMapping("/totals")
    public ResponseEntity<?> getTotal(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationToken.authenticateRequest(request, response);

        if (!tokenService.checkPermissions(request, Collections.singletonList("110"))) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized access"));
        }

        try {
            long patients = patientService.countPatients();
            long doctors = userService.countUsers();
            long images = imageService.countImages();

            return ResponseEntity.ok(Map.of("doctors", doctors, "patients", patients, "images", images));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Internal Server Error"));
        }
    }
}
