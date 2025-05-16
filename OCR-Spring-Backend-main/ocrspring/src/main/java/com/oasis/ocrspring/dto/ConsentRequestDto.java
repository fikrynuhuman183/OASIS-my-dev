package com.oasis.ocrspring.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.oasis.ocrspring.dto.subdto.Risk_factors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequestDto {
    @JsonProperty("patient_id")
    private String patientId;
    @JsonProperty("clinician_id")
    private String clinicianId;
    @JsonProperty("patient_name")
    private String patientName;
    @JsonProperty("risk_factors")
    private List<Risk_factors> riskFactors;
    @JsonProperty("DOB")
    private String dob;
    private String gender;
    @JsonProperty("histo_diagnosis")
    private String histoDiagnosis;
    @JsonProperty("medical_history")
    private List<String> medicalHistory;
    @JsonProperty("family_history")
    private List<String> familyHistory;
    @JsonProperty("systemic_disease")
    private String systemicDisease;
    @JsonProperty("contact_no")
    private String contactNo;

}