package com.oasis.ocrspring.dto;

import com.oasis.ocrspring.dto.subdto.OptionsSubDto;
import com.oasis.ocrspring.model.Option;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OptionsDto {
    private String  _id;
    private String name;
    private List<OptionsSubDto> options;
    private String createdAt;
    private String updatedAt;

    public OptionsDto(Option option) {
        this._id = option.getId();
        this.name = option.getName();
        this.options = option.getOptions();
        this.createdAt = option.getCreatedAt().toString();
        this.updatedAt = option.getUpdatedAt().toString();
    }

}
