package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.draftModels.DraftImage;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadDraftImageResponse {
    private List<DraftImage> docs;
    private String message;
}
