package com.oasis.ocrspring.dto;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Id
    private String id; // ObjectId field
    private String username;
    private String hospital;
    private String contactNo;
    private boolean availability;
}