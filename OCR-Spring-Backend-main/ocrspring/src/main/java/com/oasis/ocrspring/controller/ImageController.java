package com.oasis.ocrspring.controller;

import com.oasis.ocrspring.dto.UpdateImageRequestDto;
import com.oasis.ocrspring.service.ImageService;
import com.oasis.ocrspring.service.auth.AuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final ImageService imageService;
    private final AuthenticationToken authenticationToken;
    @Autowired
    public ImageController(ImageService imageService, AuthenticationToken authenticationToken) {
        this.imageService = imageService;
        this.authenticationToken = authenticationToken;
    }
    @PostMapping("/update")
    public ResponseEntity<Map<String, String>> updateImage(@RequestBody UpdateImageRequestDto request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        authenticationToken.authenticateRequest(httpRequest, httpResponse);
        try {
            imageService.updateImage(request);
            return ResponseEntity.ok().body( Map.of("message","Image data uploaded successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message",e.getMessage()));
        }
    }
}
