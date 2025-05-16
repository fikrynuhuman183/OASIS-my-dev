package com.oasis.ocrspring.model.draftModels;

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
import java.util.ArrayList;
import java.util.List;

@Document(collection = "draftentries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DraftEntry {
    @Id
    @Field("_id")
    @JsonIgnore
    private ObjectId id;
    @JsonProperty("_id")
    public String getIDString(){return (id != null)?id.toHexString():null;}

    @Field("patient")
    @JsonIgnore
    private ObjectId patient;
    @JsonProperty("patient")
    public String getPatientIdString(){return (patient != null)?patient.toHexString():null;}

    @Field("clinician_id")
    @JsonIgnore
    private ObjectId clinicianId;
    @JsonProperty("clinician_id")
    public String getClinicianIdString(){return (clinicianId != null)?clinicianId.toHexString():null;}

    private String complaint;

    @Field("start_time")
    @JsonProperty("start_time")
    private LocalDateTime startTime;

    @Field("end_time")
    @JsonProperty("end_time")
    private LocalDateTime endTime;

    private String findings;

    @Field("current_habits")
    @JsonProperty("current_habits")
    private List<HabitDto> currentHabits;

    private boolean updated;

    private  List<ObjectId> images = new ArrayList<>();

    private List<ObjectId> reports = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    @Override
    public String toString() {
        return "DraftEntry{" +
                "id=" + id +
                ", patient=" + patient +
                ", clinicianId=" + clinicianId +
                ", complaint='" + complaint + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", findings='" + findings + '\'' +
                ", currentHabits=" + currentHabits +
                ", updated=" + updated +
                ", images=" + images +
                ", reports=" + reports +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}