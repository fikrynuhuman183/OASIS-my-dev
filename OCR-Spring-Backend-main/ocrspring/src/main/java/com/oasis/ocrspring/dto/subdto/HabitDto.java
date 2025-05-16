package com.oasis.ocrspring.dto.subdto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitDto {
    private String habit;
    private String frequency;
}