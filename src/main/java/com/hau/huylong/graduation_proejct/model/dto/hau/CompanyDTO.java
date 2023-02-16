package com.hau.huylong.graduation_proejct.model.dto.hau;

import com.hau.huylong.graduation_proejct.model.dto.auth.UserDTO;
import com.hau.huylong.graduation_proejct.model.dto.auth.UserInfoDTO;
import lombok.Data;

import javax.persistence.Column;
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
    private String fileId;
    private String businessLicense;
    private String businessIntroduction;

    private Integer numberStudentSubmit;
    private Integer numberPositionSubmit;
    private UserInfoDTO userInfoDTO;
    private UserDTO userDTO;
}
