package com.oasis.ocrspring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oasis.ocrspring.dto.subdto.Risk_factors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @Field("_id")
    @JsonIgnore
    private ObjectId id;
    @JsonProperty("_id")
    public String getIdString(){
        return (id != null)?id.toHexString():null;
    }

    @Field("patient_id")
    private String patientId;

    @Field("clinician_id")
    @JsonIgnore
    private ObjectId clinicianId;
    @JsonProperty("clinician_id")
    public String getPatientIdString(){
        return (clinicianId != null)?clinicianId.toHexString():null;
    }

    @Field("patient_name")
    private String patientName;

    @Field("risk_factors")
    private List<Risk_factors> riskFactors;

    @Field("DOB")
    private LocalDate dob;

    @Field("gender")
    private String gender;

    @Field("histo_diagnosis")
    private String histoDiagnosis;

    @Field("medical_history")
    private List<String> medicalHistory;

    @Field("family_history")
    private List<String> familyHistory;

    @Field("systemic_disease")
    private String systemicDisease;

    @Field("contact_no")
    private String contactNo;

    @Field("consent_form")
    private String consentForm;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "Patient{" +
                "id='" + getIdString() + '\'' +
                ", patientId='" + getPatientIdString() + '\'' +
                ", clinicianId='" + clinicianId + '\'' +
                ", patient_name='" + patientName + '\'' +
                ", risk_factors=" + riskFactors +
                ", DOB=" + dob +
                ", gender='" + gender + '\'' +
                ", histo_diagnosis='" + histoDiagnosis + '\'' +
                ", medical_history=" + medicalHistory +
                ", family_history=" + familyHistory +
                ", systemic_disease='" + systemicDisease + '\'' +
                ", contact_no='" + contactNo + '\'' +
                ", consent_form='" + consentForm + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("_id", this.getId());
        map.put("patient_id", this.getPatientId());
        map.put("clinician_id", this.getClinicianId());
        map.put("patient_name", this.getPatientName());
        map.put("risk_factors", this.getRiskFactors());
        map.put("DOB", this.getDob());
        map.put("gender", this.getGender());
        map.put("histo_diagnosis", this.getHistoDiagnosis());
        map.put("medical_history", this.getMedicalHistory());
        map.put("family_history", this.getFamilyHistory());
        map.put("systemic_disease", this.getSystemicDisease());
        map.put("contact_no", this.getContactNo());
        map.put("consent_form", this.getConsentForm());
        map.put("createdAt", this.getCreatedAt());
        map.put("updatedAt", this.getUpdatedAt());
        return map;
    }
}