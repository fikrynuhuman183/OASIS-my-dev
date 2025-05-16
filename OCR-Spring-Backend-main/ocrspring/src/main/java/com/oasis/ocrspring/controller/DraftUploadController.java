package com.oasis.ocrspring.controller;

import com.oasis.ocrspring.dto.*;
import com.oasis.ocrspring.repository.draftRepos.DraftEntryRepository;
import com.oasis.ocrspring.repository.draftRepos.DraftReportRepository;
import com.oasis.ocrspring.repository.draftRepos.DraftimageRepository;
import com.oasis.ocrspring.service.auth.AuthenticationToken;
import com.oasis.ocrspring.service.auth.TokenService;
import com.oasis.ocrspring.service.draftServices.DraftImageService;
import com.oasis.ocrspring.service.draftServices.DraftReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.oasis.ocrspring.controller.UploadController.UNAUTHORIZED_ACCESS;

@RestController
@RequestMapping("/api/user/draftupload")
public class DraftUploadController {
    private final AuthenticationToken authenticationToken;
    private final TokenService tokenService;
    private final DraftImageService draftImageService;
    private final DraftReportService draftReportService;

    @Autowired
    public DraftUploadController(AuthenticationToken authenticationToken,
                                 TokenService tokenService,
                                 DraftImageService draftImageService,
                                 DraftReportService draftReportService) {
        this.authenticationToken = authenticationToken;
        this.tokenService = tokenService;
        this.draftImageService = draftImageService;
        this.draftReportService = draftReportService;
    }
    @ApiIgnore
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/swagger-ui.html");
    }

    @PostMapping("/images/{id}")
    public ResponseEntity<UploadDraftImageResponse> uploadDraftImages(HttpServletRequest request, HttpServletResponse response,
                                                                      @PathVariable String id,
                                                                      @RequestPart("data") ImageRequestDto data,
                                                                      @RequestPart("files") List<MultipartFile> files) throws IOException {

        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new UploadDraftImageResponse(null,UNAUTHORIZED_ACCESS));
        }
        String clinicianId = request.getAttribute("_id").toString();
        return draftImageService.uploadImages(data, id,clinicianId, files);
    }

    @PostMapping("/reports/{id}")
    public ResponseEntity<UploadDraftReportResponse> uploadDraftReport(HttpServletRequest request, HttpServletResponse response,
                                                                  @PathVariable String id,
                                                                  @RequestPart("data") ReportsRequestDto data,
                                                                  @RequestPart("files") List<MultipartFile> files) throws IOException{

        authenticationToken.authenticateRequest(request, response);
        if(!tokenService.checkPermissions(request, Collections.singletonList("300"))){
            return ResponseEntity.status(401).body(new UploadDraftReportResponse(null,UNAUTHORIZED_ACCESS));
        }
        String clinicianId=request.getAttribute("_id").toString();
        return draftReportService.uploadReports(data, id,clinicianId, files);
    }

}
