package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.dto.subdto.Risk_factors;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchPatientDto {
    private String id;
    private String patientId;
    private String patientName;
    private String dob;
    private String gender;
}