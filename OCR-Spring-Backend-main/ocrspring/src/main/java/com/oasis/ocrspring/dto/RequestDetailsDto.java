package com.oasis.ocrspring.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oasis.ocrspring.model.Request;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
public class RequestDetailsDto {

    private String _id;
    private String username;
    private String email;
    private String reg_no;
    private String hospital;
    private String designation;
    private String contact_no;

    public RequestDetailsDto(Request request){
        this._id = request.getId();
        this.username = request.getUserName();
        this.email = request.getEmail();
        this.reg_no = request.getRegNo();
        this.hospital = request.getHospital();
        this.designation = request.getDesignation();
        this.contact_no = request.getContactNo();
    }
}
