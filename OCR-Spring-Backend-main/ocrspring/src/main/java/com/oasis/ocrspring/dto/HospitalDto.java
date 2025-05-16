package com.oasis.ocrspring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class HospitalDto {
    @Id
    private String id;

    private String name;

    private String province;

    private String district;

    private String createdAt;
}
