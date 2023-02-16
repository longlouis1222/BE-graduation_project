package com.hau.huylong.graduation_proejct.model.request;

import lombok.Data;

import javax.persistence.Column;
import java.time.Instant;

@Data
public class SearchPostRequest extends SearchRequest {
    private Long companyId;
    private String title;
    private String recruitmentAge;
    private String workplace;
    private String necessarySkills;
    private Long industryId;
    private String recruitmentArea;
    private String recruitmentGender;
    private Instant jobApplicationDeadline;
    private Instant dateSubmit;
    private String probationaryPeriod;
    private Integer numberOfRecruits;
    private String recruitmentDegree;
    private Float recruitmentExperience;
    private Double salaryMin;
    private Double salaryMax;
    private String level;
    private String workingForm;
    private String jobDescription;
    private String jobRequirements;
    private String benefits;
    private String status;
    private Boolean isOutstanding;
}
