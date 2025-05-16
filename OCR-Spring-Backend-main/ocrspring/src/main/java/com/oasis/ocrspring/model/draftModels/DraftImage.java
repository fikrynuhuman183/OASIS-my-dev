package com.oasis.ocrspring.model.draftModels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oasis.ocrspring.dto.subdto.AnnotationDto;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "draftimages")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DraftImage
{
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

    @Field("image_name")
    private String imageName;

    private String location;

    @Field("clinical_diagnosis")
    private String clinicalDiagnosis;

    @Field("lesions_appear")
    private Boolean lesionsAppear;

    private List<AnnotationDto> annotation;

    @Field("predicted_cat")
    private String predictedCat;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}