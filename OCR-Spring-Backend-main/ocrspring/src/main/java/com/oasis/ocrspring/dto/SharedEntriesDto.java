package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.Assignment;
import com.oasis.ocrspring.model.Patient;
import com.oasis.ocrspring.model.TeleconEntry;
import com.oasis.ocrspring.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@ToString
public class SharedEntriesDto {
    @Field("_id")
    private String id;

    @Field("reviewer_id")
    private String reviewerId;

    @Field("telecon_entry")
    private PatientClinicianDto teleconEntry;

    private Boolean checked;

    private Boolean reviewed;
    private String createdAt;
    private String updatedAt;
    public SharedEntriesDto(TeleconEntry teleconEntry, Patient patient, User clinician, Assignment assignment){
        this.id = assignment.getId().toHexString();
        this.reviewerId = assignment.getReviewerId().toHexString();
        if(teleconEntry != null) {
            this.teleconEntry = new PatientClinicianDto(teleconEntry, patient, clinician);
        }else {
            this.teleconEntry = null;
        }
        this.checked = assignment.getChecked();
        this.reviewed = assignment.getReviewed();
        this.createdAt = String.valueOf(assignment.getCreatedAt());
        this.updatedAt = String.valueOf(assignment.getUpdatedAt());

    }
}
