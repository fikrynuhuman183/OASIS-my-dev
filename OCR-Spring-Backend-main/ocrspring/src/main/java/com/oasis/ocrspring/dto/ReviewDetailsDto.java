package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.Review;
import com.oasis.ocrspring.model.User;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
@Data
@Setter
@Getter
@NoArgsConstructor
@ToString
public class ReviewDetailsDto {
    private String id;

    @Field("telecon_entry_id")
    private String teleconEntryId;

    @Field("reviewer_id")
    private ReviewerDetailsDto_ reviewerId;

    @Field("provisional_diagnosis")
    private String provisionalDiagnosis;

    @Field("management_suggestions")
    private String managementSuggestions;

    @Field("referral_suggestions")
    private String referralSuggestions;

    @Field("other_comments")
    private String otherComments;

    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private LocalDateTime updatedAt;

    public ReviewDetailsDto(Review review , User reviewer){
        this.id = review.getId().toHexString();
        this.teleconEntryId = review.getTeleconEntryId().toHexString();
        this.reviewerId = new ReviewerDetailsDto_(reviewer);
        this.provisionalDiagnosis =review.getProvisionalDiagnosis();
        this.managementSuggestions = review.getManagementSuggestions();
        this.referralSuggestions =review.getReferralSuggestions();
        this.otherComments = review.getOtherComments();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }
}
