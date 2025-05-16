package com.oasis.ocrspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminSignUpRequestDto {
    private String username;

    private String email;

    @Field("reg_no")
    @JsonProperty("reg_no")
    private String regNo;

    private String hospital;
}
