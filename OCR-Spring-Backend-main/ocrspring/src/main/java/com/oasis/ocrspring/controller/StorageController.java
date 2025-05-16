package com.oasis.ocrspring.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/Storage")
public class StorageController {

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
}
