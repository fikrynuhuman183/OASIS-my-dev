package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDetailsDto {
    private String id;
    private String patientId;
    private String patientName;

    public PatientDetailsDto(Patient patient) {
        this.id = patient.getId().toString();
        this.patientId = patient.getPatientId();
        this.patientName = patient.getPatientName();
    }
}