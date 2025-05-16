package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerDetailsDto {
    private String id;
    private String username;

    public ReviewerDetailsDto(User reviewer) {
        this.id = String.valueOf(reviewer.getId());
        this.username = reviewer.getUsername();
    }
}