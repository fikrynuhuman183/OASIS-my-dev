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
@ToString
@AllArgsConstructor
public class PatientDetailsResDto {
    private String systemic_disease;
    private String _id;
    private String patient_id;
    private String clinician_id;
    private String dob;
    private String patient_name;
    private List<Risk_factors> risk_factors;
    private String gender;
    private String histo_diagnosis;
    private List<String> medical_history;
    private List<String> family_history;
    private String contact_no;
    private String consent_form;
    private LocalDateTime  createdAt;
    private LocalDateTime updatedAt;



}
