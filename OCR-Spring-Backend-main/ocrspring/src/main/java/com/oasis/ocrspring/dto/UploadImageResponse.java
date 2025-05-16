package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadImageResponse {
    private List<Image> docs;
    private String message;
}