package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.Report;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadReportResponse {
    private List<Report> docs;
    private String message;
}