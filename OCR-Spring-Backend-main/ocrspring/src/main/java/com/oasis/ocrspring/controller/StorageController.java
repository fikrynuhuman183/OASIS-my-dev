package com.oasis.ocrspring.controller;

import com.oasis.ocrspring.dto.ImageResponseDto;
import com.oasis.ocrspring.model.Image;
import com.oasis.ocrspring.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/Storage")
public class StorageController {

    @Autowired
    private ImageService imageService;

    @ApiIgnore
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/swaggr-ui.html");
    }

    @GetMapping
    public ResponseEntity<byte[]> getStorage() {
        // Implement logic to serve static files from /Storage directory
        return ResponseEntity.ok().body(new byte[0]);
    }

    @GetMapping("/images")
    public ResponseEntity<byte[]> getImages() {
        // Implement logic to serve static files from /Storage/images directory
        return ResponseEntity.ok().body(new byte[0]);
    }

    @GetMapping("/reports")
    public ResponseEntity<byte[]> getReports() {
        // Implement logic to serve static files from /Storage/reports directory
        return ResponseEntity.ok().body(new byte[0]);
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<?> getImageDetails(@PathVariable String id) {
        Optional<Image> imageOpt = imageService.getImageById(id);
        if (imageOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Image not found");
        }
        Image image = imageOpt.get();
        ImageResponseDto dto = new ImageResponseDto();
        dto.setId(image.getId().toHexString());
        dto.setTeleconEntryId(image.getTeleconEntryId() != null ? image.getTeleconEntryId().toHexString() : null);
        dto.setImageName(image.getImageName());
        dto.setLocation(image.getLocation());
        dto.setFileUri(image.getFileUri());
        dto.setClinicalDiagnosis(image.getClinicalDiagnosis());
        dto.setLesionsAppear(image.getLesionsAppear());
        dto.setAnnotation(image.getAnnotation());
        dto.setPredictedCat(image.getPredictedCat());
        dto.setFileSize(image.getFileSize());
        dto.setContentType(image.getContentType());
        dto.setCreatedAt(image.getCreatedAt());
        dto.setUpdatedAt(image.getUpdatedAt());
        return ResponseEntity.ok(dto);
    }
}
