package com.oasis.ocrspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oasis.ocrspring.dto.subdto.HabitDto;
import com.oasis.ocrspring.model.draftModels.DraftEntry;
import lombok.*;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DraftEntryResponseDto {
    @JsonProperty("_id")
    private String id;

    @JsonProperty("patient")
    private PatientDetailsDto patient;

    @JsonProperty("clinician_id")
    private String clinicianId;

    @JsonProperty("complaint")
    private String complaint;

    @JsonProperty("start_time")
    private LocalDateTime startTime;

    @JsonProperty("end_time")
    private LocalDateTime endTime;

    @JsonProperty("findings")
    private String findings;

    @JsonProperty("current_habits")
    private List<HabitDto> currentHabits;

    @JsonProperty("updated")
    private boolean updated;

    @JsonProperty("images")
    private  List<String> images ;

    @JsonProperty("reports")
    private List<String> reports ;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt ;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    public DraftEntryResponseDto(DraftEntry draftEntry, PatientDetailsDto patientDetails){
        this.id = draftEntry.getId().toHexString();
        this.patient = patientDetails;
        this.clinicianId = draftEntry.getClinicianId().toHexString();
        this.complaint = draftEntry.getComplaint();
        this.startTime = draftEntry.getStartTime();
        this.endTime = draftEntry.getEndTime();
        this.findings = draftEntry.getFindings();
        this.currentHabits = draftEntry.getCurrentHabits();
        this.updated = draftEntry.isUpdated();
        this.images = Collections.unmodifiableList(draftEntry.getImages().stream().map(ObjectId::toHexString).toList());
        this.reports = Collections.unmodifiableList(draftEntry.getReports().stream().map(ObjectId::toHexString).toList());
        this.createdAt = draftEntry.getCreatedAt();
        this.updatedAt = draftEntry.getUpdatedAt();
    }

}
