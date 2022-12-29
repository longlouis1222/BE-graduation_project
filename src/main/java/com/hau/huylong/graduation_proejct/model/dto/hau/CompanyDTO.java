package com.hau.huylong.graduation_proejct.model.dto.hau;

import lombok.Data;

import java.util.Date;

@Data
public class CompanyDTO {
    private Long id;
    private String createdBy;
    private Date created;
    private String updatedBy;
    private Date updated;
    private String taxCode;
    private String name;
    private String employeeNumber;
    private String companyAddress;
    private String companyPhoneNumber;
    private String location;
    private String fieldOfActivity;
    private Integer userId;
}
