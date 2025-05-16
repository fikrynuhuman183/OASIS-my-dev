package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.Patient;
import com.oasis.ocrspring.model.TeleconEntry;
import com.oasis.ocrspring.model.User;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PatientClinicianDto {
    @Field("_id")
    private String id;
    private PatientDetailsDto patient;
    @Field("clinician_id")
    private ClinicianDetailsDto clinician;
    public PatientClinicianDto(TeleconEntry teleconEntry, Patient patient, User clinician){
        this.id = teleconEntry.getId().toHexString();
        this.patient =new PatientDetailsDto(patient);
        this.clinician = new ClinicianDetailsDto(clinician);
    }
}
