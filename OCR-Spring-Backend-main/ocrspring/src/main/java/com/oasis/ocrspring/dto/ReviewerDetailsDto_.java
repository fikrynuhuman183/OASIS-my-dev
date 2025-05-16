package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.User;
import lombok.*;

@Data
@ToString
@Getter
@Setter
@NoArgsConstructor
public class ReviewerDetailsDto_ {
    private String id;
    private String username;
    private String reg_no;

    public ReviewerDetailsDto_(User reviewer) {
        this.id = String.valueOf(reviewer.getId());
        this.username = reviewer.getUsername();
        this.reg_no = reviewer.getRegNo();
    }
}
