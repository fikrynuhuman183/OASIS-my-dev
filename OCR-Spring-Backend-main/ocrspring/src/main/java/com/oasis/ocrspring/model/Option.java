package com.oasis.ocrspring.model;

import com.oasis.ocrspring.dto.subdto.OptionsSubDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "options")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Option {
    @Id
    private String id;

    private String name;

    private List<OptionsSubDto> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Option(String name, List<OptionsSubDto> options) {
        this.name = name;
        this.options = options;
        this.createdAt = LocalDateTime.now();
    }

}