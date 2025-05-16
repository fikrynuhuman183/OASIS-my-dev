package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.dto.subdto.HabitDto;
import com.oasis.ocrspring.model.TeleconEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientTeleconResponse {
    private String id; // MongoDB typically uses String for IDs
    private String patient;
    private String clinicianId;
    private String complaint;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String findings;
    private List<HabitDto> currentHabits;
    private boolean updated;
    private List<ObjectId> reviewers;
    private List<String> reviews;
    private List<ObjectId> images;
    private List<ObjectId> reports;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PatientTeleconResponse(TeleconEntry teleconEntry) {
        this.id = teleconEntry.getId().toString();
        this.patient = teleconEntry.getPatient().toString();
        this.clinicianId = teleconEntry.getClinicianId().toString();
        this.complaint = teleconEntry.getComplaint();
        this.startTime = teleconEntry.getStartTime();
        this.endTime = teleconEntry.getEndTime();
        this.findings = teleconEntry.getFindings();
        this.currentHabits = teleconEntry.getCurrentHabits();
        this.updated = teleconEntry.isUpdated();
        this.reviewers = teleconEntry.getReviewers();
        this.reviews = teleconEntry.getReviews();
        this.images = teleconEntry.getImages();
        this.reports = teleconEntry.getReports();
        this.createdAt = teleconEntry.getCreatedAt();
        this.updatedAt = teleconEntry.getUpdatedAt();
    }
}