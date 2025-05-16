package com.oasis.ocrspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oasis.ocrspring.model.User;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminSignUpResponse {
    @JsonProperty("_id")
    private String id;

    private String username;

    private String email;

    @Field("reg_no")
    @JsonProperty("reg_no")
    private String regNo;

    private String hospital;

    private String designation;

    @Field("contact_no")
    @JsonProperty("contact_no")
    private String contactNo;

    private boolean availability;

    private String role;

    @CreatedDate
    private LocalDateTime createdAt ;

    @LastModifiedDate
    private LocalDateTime updatedAt ;

    private String message;

    public AdminSignUpResponse(User user, String message){
        this.id = user.getId().toHexString();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.regNo = user.getRegNo();
        this.hospital = user.getHospital();
        this.designation = user.getDesignation();
        this.contactNo = user.getContactNo();
        this.availability = user.isAvailable();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.message = message;
    }



}
