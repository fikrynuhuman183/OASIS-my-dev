package com.oasis.ocrspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oasis.ocrspring.dto.subdto.AnnotationDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequestDto {
    @JsonProperty("telecon_entry_id")
    private ObjectId teleconId;
    @JsonProperty("image_name")
    private String imageName;
    private String location;
    @JsonProperty("clinical_diagnosis")
    private String clinicalDiagnosis;
    @JsonProperty("lesions_appear")
    private Boolean lesionsAppear;
    private List<AnnotationDto> annotation;
    @JsonProperty("predicted_cat")
    private String predictedCat;

}