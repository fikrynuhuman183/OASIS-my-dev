package com.oasis.ocrspring.dto.subdto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Risk_factors {
    private String habit;
    private String frequency;
    private String duration;
}