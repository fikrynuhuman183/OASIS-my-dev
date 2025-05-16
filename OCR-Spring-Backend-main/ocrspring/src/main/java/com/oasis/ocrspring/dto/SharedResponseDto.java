package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.dto.subdto.Risk_factors;
import com.oasis.ocrspring.model.Patient;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;


import java.util.List;
@Getter
@Setter
public class SharedResponseDto {

    private String  _id;
    private String patient_id;
    private String  clinician_id;
    private String patient_name;
    private List<Risk_factors> risk_factors;
    private String  DOB;
    private String gender;
    private String histo_diagnosis;
    private List<String> medical_history;
    private List<String> family_history;
    private String systemic_disease;
    private String contact_no;
    private String consent_form;
    private String createdAt;
    private String updatedAt;

    public SharedResponseDto(Patient patient) {
        this._id = patient.getId().toString();
        this.patient_id = patient.getPatientId();
        this.clinician_id = patient.getClinicianId().toString();
        this.patient_name = patient.getPatientName();
        this.risk_factors = patient.getRiskFactors();
        this.DOB = patient.getDob().toString();
        this.gender = patient.getGender();
        this.histo_diagnosis = patient.getHistoDiagnosis();
        this.medical_history = patient.getMedicalHistory();
        this.family_history = patient.getFamilyHistory();
        this.systemic_disease = patient.getSystemicDisease();
        this.contact_no = patient.getContactNo();
        this.consent_form = patient.getConsentForm();
        this.createdAt = patient.getCreatedAt().toString();
        this.updatedAt = patient.getUpdatedAt().toString();
    }
}
