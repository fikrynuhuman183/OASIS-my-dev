package com.oasis.ocrspring.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Document(collection = "assignments")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {
    @Id
    @Field("_id")
    private ObjectId id;

    @Field("reviewer_id")
    private ObjectId reviewerId;

    @Field("telecon_entry")
    private ObjectId teleconEntry;

    private Boolean checked;

    private Boolean reviewed;

    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

}
