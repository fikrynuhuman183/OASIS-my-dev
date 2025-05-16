package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.User;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ClinicianDetailsDto {
    @Field("_id")
    private String id;
    private String username;
    @Field("reg_no")
    private String regNo;
    public ClinicianDetailsDto(User clinician){
        this.id = clinician.getId().toHexString();
        this.username = clinician.getUsername();
        this.regNo = clinician.getRegNo();
    }
}
