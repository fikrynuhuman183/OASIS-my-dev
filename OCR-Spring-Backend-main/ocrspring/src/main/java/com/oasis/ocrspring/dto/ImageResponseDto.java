package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.dto.subdto.AnnotationDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDto {
    private String id;
    private String teleconEntryId;
    private String imageName;
    private String location;
    private String fileUri;
    private String clinicalDiagnosis;
    private Boolean lesionsAppear;
    private List<AnnotationDto> annotation;
    private String predictedCat;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}