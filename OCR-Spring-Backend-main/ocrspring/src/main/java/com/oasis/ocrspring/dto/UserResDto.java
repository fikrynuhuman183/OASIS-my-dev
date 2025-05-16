package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResDto {
    private String username;
    private String email;
    private String reg_no;
    private String hospital;
    private String designation;
    private String contact_no;
    private boolean availability;
    private String role;
    private String _id;
    private String createdAt;
    private String updatedAt;
    private String message;

    public UserResDto(String username, String email, String reg_no, String hospital, String designation, String contact_no, boolean availability, String role, String _id, String createdAt, String message) {
        this.username = username;
        this.email = email;
        this.reg_no = reg_no;
        this.hospital = hospital;
        this.designation = designation;
        this.contact_no = contact_no;
        this.availability = availability;
        this.role = role;
        this._id = _id;
        this.createdAt = createdAt;
        this.message = message;
    }
    public UserResDto(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.reg_no = user.getRegNo();
        this.hospital = user.getHospital();
        this.designation = user.getDesignation();
        this.contact_no = user.getContactNo();
        this.availability = user.isAvailability();
        this.role = user.getRole();
        this._id = user.getId().toString();
        this.createdAt = user.getCreatedAt().toString();
        this.updatedAt = user.getUpdatedAt().toString();
    }
}
