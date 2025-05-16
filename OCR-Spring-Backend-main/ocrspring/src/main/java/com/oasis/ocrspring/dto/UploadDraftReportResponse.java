package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.draftModels.DraftReport;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UploadDraftReportResponse {
    private List<DraftReport> docs;
    private String message;
}
