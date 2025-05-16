package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.dto.subdto.HabitDto;
import com.oasis.ocrspring.model.Image;
import com.oasis.ocrspring.model.Report;
import com.oasis.ocrspring.model.TeleconEntry;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PopulatedEntryDto {
    private String id; // MongoDB typically uses String for IDs
    private PatientDetailsDto patient;
    private String clinicianId;
    private String complaint;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String findings;
    private List<HabitDto> currentHabits;
    private boolean updated;
    private List<ReviewerDetailsDto_> reviewers;
    private List<String> reviews;
    private List<Image> imageDetails;
    private List<Report> reportDetails;
    @CreatedDate
    @Field("createdAt")
    private ZonedDateTime  createdAt;
    @LastModifiedDate
    @Field("updatedAt")
    private ZonedDateTime updatedAt;
    public PopulatedEntryDto(TeleconEntry teleconEntry, PatientDetailsDto patient, List<ReviewerDetailsDto_> reviewer,
                             List<Image> imageDetails, List<Report> reportDetails) {
        this.id = teleconEntry.getId().toString();
        this.patient = patient;
        this.clinicianId = teleconEntry.getClinicianId().toString();
        this.complaint = teleconEntry.getComplaint();
        this.startTime = teleconEntry.getStartTime();
        this.endTime = teleconEntry.getEndTime();
        this.findings = teleconEntry.getFindings();
        this.currentHabits = teleconEntry.getCurrentHabits();
        this.updated = teleconEntry.isUpdated();
        this.reviewers = reviewer;
        this.reviews = teleconEntry.getReviews();
        this.imageDetails = imageDetails;
        this.reportDetails = reportDetails;
        this.createdAt = teleconEntry.getCreatedAt().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
        this.updatedAt = teleconEntry.getUpdatedAt().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
    }
}
