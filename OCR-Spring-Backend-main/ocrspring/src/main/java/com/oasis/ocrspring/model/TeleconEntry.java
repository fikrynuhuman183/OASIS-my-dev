package com.oasis.ocrspring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oasis.ocrspring.dto.subdto.HabitDto;
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
import java.util.List;

@Document(collection = "teleconentries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeleconEntry
{
    @Id
    @Field("_id")
    @JsonIgnore
    private ObjectId id;
    @JsonProperty("_id")
    public String getIDString(){return (id != null)?id.toHexString():null;}

    @JsonIgnore
    @Field("patient")
    private ObjectId patient;
    @JsonProperty("patient")
    public  String getPatientString(){return (patient != null)?patient.toHexString():null;}

    @Field("clinician_id")
    @JsonIgnore
    private ObjectId clinicianId;
    @JsonProperty("clinician_id")
    public String getClinicianIdString(){return (clinicianId != null)?clinicianId.toHexString():null;}

    private String complaint;

    @Field("start_time")
    private LocalDateTime startTime;

    @Field("end_time")
    private LocalDateTime endTime;

    private String findings;

    private String status;

    @Field("current_habits")
    private List<HabitDto> currentHabits;

    private boolean updated;

    private List<ObjectId> reviewers;

    private List<String> reviews;

    private List<ObjectId> images;

    private List<ObjectId> reports;

    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    @Override
    public String toString() {
        return "TeleconEntry{" +
                "id=" + getIDString() +
                ", patient=" + getPatientString() +
                ", clinicianId=" + getClinicianIdString() +
                ", complaint='" + complaint + '\'' +
                ", start_time=" + startTime +
                ", end_time=" + endTime +
                ", findings='" + findings + '\'' +
                ", status='" + status + '\'' +
                ", current_habits=" + currentHabits +
                ", updated=" + updated +
                ", reviewers=" + reviewers +
                ", reviews=" + reviews +
                ", images=" + images +
                ", reports=" + reports +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }


}