package com.oasis.ocrspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportsRequestDto {
    @JsonProperty("telecon_entry_id")
    private ObjectId teleconId;
    @JsonProperty("report_name")
    private String reportName;
}