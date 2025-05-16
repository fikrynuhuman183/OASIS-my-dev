package com.oasis.ocrspring.model.draftModels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "draftreports")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DraftReport {
    @Id
    @Field("_id")
    @JsonIgnore
    private ObjectId id;

    @JsonProperty("_id")
    public String getIdString() {
        return id != null ? id.toHexString() : null;
    }

    @Field("telecon_entry_id")
    @JsonIgnore
    private ObjectId teleconEntryId;
    @JsonProperty("telecon_entry_id")
    public String getTeleconIdString(){return teleconEntryId != null? teleconEntryId.toHexString() : null;}

    @Field("report_name")
    private String reportName;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}