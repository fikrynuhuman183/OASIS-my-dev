package com.oasis.ocrspring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReqToUserDto {

    private String username;
    private String email;
    private String reg_no;
    private String role;
    private String hospital;
    private String designation;
    private String contact_no;
    private String availability;
    private String reason;


}
