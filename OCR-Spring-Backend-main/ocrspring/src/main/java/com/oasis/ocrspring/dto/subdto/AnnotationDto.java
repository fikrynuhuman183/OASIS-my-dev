package com.oasis.ocrspring.dto.subdto;

import lombok.*;

import java.util.List;
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AnnotationDto {
    private Integer id;
    private String name ;
    private List<Integer> annotations;
    private List<Integer> bbox;
}
