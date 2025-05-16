package com.oasis.ocrspring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Document(collection = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @Field("_id")
    @JsonIgnore
    private ObjectId id;
    @JsonProperty("_id")
    public String getIDString(){return (id != null)?id.toHexString():null;}

    @Field("telecon_entry_id")
    @JsonIgnore
    private ObjectId teleconEntryId;
    @JsonProperty("telecon_entry_id")
    public String getTeleconId(){return ((teleconEntryId != null)?teleconEntryId.toHexString():null);}

    @Field("reviewer_id")
    @JsonIgnore
    private ObjectId reviewerId;
    @JsonProperty("reviewer_id")
    @Field("reviewer_id")
    public String getReviewerId(){return ((reviewerId != null)?reviewerId.toHexString():null);}

    @JsonProperty("provisional_diagnosis")
    @Field("provisional_diagnosis")
    private String provisionalDiagnosis;

    @JsonProperty("management_suggestions")
    @Field("management_suggestions")
    private String managementSuggestions;

    @JsonProperty("referral_suggestions")
    @Field("referral_suggestions")
    private String referralSuggestions;

    @JsonProperty("other_comments")
    @Field("other_comments")
    private String otherComments;

    @CreatedDate
    @JsonProperty("createdAt")
    private LocalDateTime createdAt = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    @LastModifiedDate
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    @Override
    public String toString() {
        return "Review{" +
                "_id=" + getIDString() +
                ", telecon_entry_id=" + getTeleconId() +
                ", reviewer_id=" + getReviewerId() +
                ", provisional_diagnosis='" + provisionalDiagnosis + '\'' +
                ", management_suggestions='" + managementSuggestions + '\'' +
                ", referral_suggestions='" + referralSuggestions + '\'' +
                ", other_comments='" + otherComments + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}