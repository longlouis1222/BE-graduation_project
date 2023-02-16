package com.hau.huylong.graduation_proejct.model.dto.hau;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class IndustryDTO {
    private Long id;
    private String createdBy;
    private Date created;
    private String updatedBy;
    private Date updated;
    private String code;
    private String name;
    private String description;
    private Integer numberSummit;
}
