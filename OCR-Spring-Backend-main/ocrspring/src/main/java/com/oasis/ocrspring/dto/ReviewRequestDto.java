package com.oasis.ocrspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    @JsonProperty("provisional_diagnosis")
    private String provisionalDiagnosis;

    @JsonProperty("management_suggestions")
    private String managementSuggestions;

    @JsonProperty("referral_suggestions")
    private String referralSuggestions;

    @JsonProperty("other_comments")
    private String otherComments;
}
