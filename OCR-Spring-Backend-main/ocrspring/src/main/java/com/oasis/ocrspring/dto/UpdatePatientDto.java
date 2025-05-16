package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.dto.subdto.Risk_factors;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePatientDto {
    private String patient_name;
    private String gender;
    private String dob;
    private List<Risk_factors> risk_factors;
    private String histo_diagnosis;
    private String contact_no;
    private String systemic_disease;
    private List<String> family_history;
    private List<String> medical_history;
}