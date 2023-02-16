package com.hau.huylong.graduation_proejct.model.dto.hau;

import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
public class PostDTO {
    private Long id;
    private String createdBy;
    private Date created;
    private String updatedBy;
    private Date updated;
    private Long industryId;
    private String title;
    private String recruitmentAge;
    private String workplace;
    private String necessarySkills;
    private String recruitmentArea;
    private String recruitmentGender;
    private Instant jobApplicationDeadline;
    private Instant dateSubmit;
    private String probationaryPeriod;
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
    private Boolean isOutstanding;
    private Long companyId;
    private CompanyDTO companyDTO;
    private IndustryDTO industryDTO;
    private boolean userCurrentSaved;
    private boolean userCurrentSubmited;
    private Integer view;
}
