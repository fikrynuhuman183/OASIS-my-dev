package com.oasis.ocrspring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto {
    private String reg_no;
    private String username;
    private String email;
    private String hospital;
    private String designation;
    private String contact_no;
    

}
