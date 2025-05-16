package com.oasis.ocrspring.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@RestController
public class FileController {

    private static final Logger logger = Logger.getLogger(FileController.class.getName());
    @Value("src/main/Storage/ConsentForms/")
    private String uploadDir;

    @GetMapping("/Storage/ConsentForms/{filename}")
    public ResponseEntity<InputStreamResource> getPdf(@PathVariable String filename) throws IOException {
        File file = new File(uploadDir +"/"+ filename+".pdf"); // Update with actual file path
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}