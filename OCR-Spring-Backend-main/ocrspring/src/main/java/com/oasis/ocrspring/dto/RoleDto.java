package com.oasis.ocrspring.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RoleDto {
    private String role;
    private List<Integer> permissions = new ArrayList<>();
}
