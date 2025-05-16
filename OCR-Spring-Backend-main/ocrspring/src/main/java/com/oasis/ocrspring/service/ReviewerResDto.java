package com.oasis.ocrspring.service;

import com.oasis.ocrspring.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ReviewerResDto {
    private String _id;
    private String username;
    private String reg_no;

    public ReviewerResDto(User user) {
        this._id = user.getId().toString();
        this.username = user.getUsername();
        this.reg_no = user.getRegNo();
    }

}
