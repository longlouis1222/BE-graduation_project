package com.hau.huylong.graduation_proejct.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class SearchRecruitmentProfileRequest extends SearchRequest {
    private String fileId;
    private String positionOffer;
    private String levelDesire;
    private String academyLevel;
    private String career;
    private String workForm;
    private String currentLevel;
    private Float offerSalary;
    private Float experienceNumber;
    private String workAddress;
    private Long userId;
    private String address;
    private String careerTarget;
    private String sortSkill;
    private Boolean permissionSearch;
    private Integer view;
    private Date timeSubmit;
}
