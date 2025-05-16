package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.dto.subdto.HabitDto;
import com.oasis.ocrspring.model.TeleconEntry;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PopulatedTeleconsultationEntry {
    private String id; // MongoDB typically uses String for IDs
    private PatientDetailsDto patient;
    private String clinicianId;
    private String complaint;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String findings;
    private List<HabitDto> currentHabits;
    private boolean updated;
    private List<String> reviewers;
    private List<String> reviews;
    private List<String> images;
    private List<String> reports;
    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Field("updatedAt")
    private LocalDateTime updatedAt;

    public PopulatedTeleconsultationEntry(TeleconEntry teleconEntry, PatientDetailsDto patient){
        this.id = teleconEntry.getId().toString();
        this.patient = patient;
        this.clinicianId = teleconEntry.getClinicianId().toString();
        this.complaint = teleconEntry.getComplaint();
        this.startTime = teleconEntry.getStartTime();
        this.endTime = teleconEntry.getEndTime();
        this.findings = teleconEntry.getFindings();
        this.currentHabits = teleconEntry.getCurrentHabits();
        this.updated = teleconEntry.isUpdated();
        this.reviewers = teleconEntry.getReviewers().stream().map(ObjectId::toHexString).toList();
        this.reviews = teleconEntry.getReviews();
        this.images =  teleconEntry.getImages().stream().map(ObjectId::toHexString).toList();
        this.reports =  teleconEntry.getReports().stream().map(ObjectId::toHexString).toList();
        this.createdAt = teleconEntry.getCreatedAt();
        this.updatedAt = teleconEntry.getUpdatedAt();
    }
}
