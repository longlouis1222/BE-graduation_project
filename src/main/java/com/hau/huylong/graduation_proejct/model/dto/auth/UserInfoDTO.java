package com.hau.huylong.graduation_proejct.model.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class UserInfoDTO {
    private Long id;
    private String fullName;
    private String avatar;
    private Instant dateOfBirth;
    private String town;
    private short gender;
    private String marriageStatus;
    private Integer userId;
    private String address;
    private Long companyId;
    private String phoneNumber;
    private String arrRecruitmentIds;
    private List<Long> arrProfileIds;
}
