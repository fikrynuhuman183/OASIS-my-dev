package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.Request;
import com.oasis.ocrspring.model.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestResDetailsDto {
    private String _id;
    private String username;
    private String reg_no;
    private String hospital;

    public RequestResDetailsDto(Request request){
        this._id =request.getId();
        this.username =request.getUserName();
        this.reg_no = request.getRegNo();
        this.hospital=request.getHospital();
    }
}
