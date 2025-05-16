package com.oasis.ocrspring.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserNameAndRoleDto {
    private String username;
    private String role;
}
