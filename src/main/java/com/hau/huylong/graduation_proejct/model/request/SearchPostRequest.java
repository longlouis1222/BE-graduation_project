package com.hau.huylong.graduation_proejct.model.request;

import lombok.Data;

import java.time.Instant;

@Data
public class SearchPostRequest extends SearchRequest {
    private Long industryId;
    private String recruitmentArea;
    private String recruitmentGender;
    private Instant jobApplicationDeadline;
    private Instant dateSubmit;
    private Instant probationaryPeriod;
    private Integer numberOfRecruits;
    private String recruitmentDegree;
    private String recruitmentExperience;
    private Double salaryMin;
    private Double salaryMax;
    private String level;
    private String workingForm;
    private String jobDescription;
    private String jobRequirements;
    private String benefits;
    private String status;
}
