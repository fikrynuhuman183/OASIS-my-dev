package com.oasis.ocrspring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Document(collection = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @JsonIgnore
    @Field("_id")
    private ObjectId id;
    @JsonProperty("_id")
    public String getIDString(){return (id != null)?id.toHexString():null;}

    private String username ="";

    private String email;

    @Field("reg_no")
    @JsonProperty("reg_no")
    private String regNo;

    private String hospital;

    @Field("designation")
    @JsonProperty("designation")
    private String designation ="";

    @Field("contact_no")
    @JsonProperty("contact_no")
    private String contactNo = "";

    private String password;

    private boolean availability = true;

    private String role;

    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    public User(String username,
                String email,
                String regNo,
                String hospital,
                String designation,
                String contactNo,
                String password,
                boolean availability,
                String role) {
        this.username = username;
        this.email = email;
        this.regNo = regNo;
        this.hospital = hospital;
        this.designation = designation;
        this.contactNo = contactNo;
        this.password = password;
        this.availability = availability;
        this.role = role;
    }


    public User(String username,
                String email,
                String regNo,
                String role,
                String hospital,
                String designation,
                String contactNo,
                boolean availability) {
        this.username = username;
        this.email = email;
        this.regNo = regNo;
        this.role = role;
        this.hospital = hospital;
        this.designation = designation;
        this.contactNo = contactNo;
        this.availability = availability;
        this.createdAt = LocalDateTime.now();
    }


    public boolean isAvailable() {
        return availability;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getIDString() +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", regNo='" + regNo + '\'' +
                ", hospital='" + hospital + '\'' +
                ", designation='" + designation + '\'' +
                ", contactNo='" + contactNo + '\'' +
                ", password='" + password + '\'' +
                ", availability=" + availability +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}