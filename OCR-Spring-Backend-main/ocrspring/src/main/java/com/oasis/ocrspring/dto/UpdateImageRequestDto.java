package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.dto.subdto.AnnotationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateImageRequestDto {
    private String _id;
    private String location;
    private String clinical_diagnosis;
    private Boolean lesions_appear;
    private List<AnnotationDto> annotation;
}
