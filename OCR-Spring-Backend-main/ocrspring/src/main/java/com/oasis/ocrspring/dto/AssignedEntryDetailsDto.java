package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.dto.subdto.HabitDto;
import com.oasis.ocrspring.model.Image;
import com.oasis.ocrspring.model.Report;
import com.oasis.ocrspring.model.TeleconEntry;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AssignedEntryDetailsDto {
    private String id; // MongoDB typically uses String for IDs
    private PatientDetailsDto patient;
    @Field("clinician_id")
    private ClinicianDetailsDto clinicianId;
    private String complaint;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String findings;
    private String status;
    private List<HabitDto> currentHabits;
    private boolean updated;
    private List<String> reviewers;
    private List<String> reviews;
    private List<Image> imageDetails;
    private List<Report> reportDetails;
    @CreatedDate
    @Field("createdAt")
    private ZonedDateTime createdAt;
    @LastModifiedDate
    @Field("updatedAt")
    private ZonedDateTime updatedAt;
    private LocalDateTime assignedAt;
    private Boolean reviewed; // Additional field from Assignment
    private Boolean checked;

    public AssignedEntryDetailsDto(TeleconEntry teleconEntry, PatientDetailsDto patient, ClinicianDetailsDto clinician,
                             List<Image> imageDetails, List<Report> reportDetails) {
        this.id = teleconEntry.getId().toString();
        this.patient = patient;
        this.clinicianId = clinician;
        this.complaint = teleconEntry.getComplaint();
        this.startTime = teleconEntry.getStartTime();
        this.endTime = teleconEntry.getEndTime();
        this.findings = teleconEntry.getFindings();
        this.status = teleconEntry.getStatus();
        this.currentHabits = teleconEntry.getCurrentHabits();
        this.updated = teleconEntry.isUpdated();
        this.reviewers = teleconEntry.getReviewers().stream().map(ObjectId::toHexString).toList();
        this.reviews = teleconEntry.getReviews();
        this.imageDetails = imageDetails;
        this.reportDetails = reportDetails;
        this.createdAt = teleconEntry.getCreatedAt().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
        this.updatedAt = teleconEntry.getUpdatedAt().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
    }
}
