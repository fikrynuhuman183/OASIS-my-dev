package com.oasis.ocrspring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

@Document(collection = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {
    @Id
    @Field("_id")
    @JsonIgnore
    private ObjectId id;
    @JsonProperty("_id")
    public String getIDString(){return id != null? id.toHexString():null;}

    @JsonIgnore
    @Field("telecon_entry_id")
    private ObjectId teleconId;
    @JsonProperty("telecon_entry_id")
    public String getTeleconEntryIdString(){ return teleconId != null? teleconId.toHexString():null;}

    @Field("report_name")
    private String reportName;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "Report{" +
                "telecon_entry_id=" + getTeleconEntryIdString() +
                ", report_name='" + reportName + '\'' +
                ", id=" + getIDString() +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';

    }
}