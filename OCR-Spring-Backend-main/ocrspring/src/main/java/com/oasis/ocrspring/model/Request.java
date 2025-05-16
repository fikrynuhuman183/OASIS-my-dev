package com.oasis.ocrspring.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "requests")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Request {
    @Id
    private String id;

    @Field("username")
    private String userName;

    private String email;

    @Field("reg_no")
    private String regNo;

    private String hospital;

    private String designation;

    @Field("contact_no")
    private String contactNo;

    public Request(String userName, String email, String regNo, String hospital,
                   String designation, String contactNo) {
        this.userName = userName;
        this.email = email;
        this.regNo = regNo;
        this.hospital = hospital;
        this.designation = designation;
        this.contactNo = contactNo;
    }
}