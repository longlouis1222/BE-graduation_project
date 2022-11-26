package com.hau.huylong.graduation_proejct.entity.hau;

import com.hau.huylong.graduation_proejct.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;

@Data
@Entity
@Table(name = "posts")
public class PostEntity extends BaseEntity {
    @Column(name = "industry")
    private String industry;

    @Column(name = "recruitment_area")
    private String recruitmentArea;

    @Column(name = "recruitment_gender")
    private String recruitmentGender;

    //thời hạn nộp hồ sơ
    @Column(name = "job_application_deadline")
    private Instant jobApplicationDeadline;

    //thời hạn đăng bài
    @Column(name = "date_submit")
    private Instant dateSubmit;

    //thời hạn thử việc
    @Column(name = "probationary_period")
    private Instant probationaryPeriod;

    @Column(name = "number_of_recruits")
    private Integer numberOfRecruits;

    // yêu cầu bằng cấp
    @Column(name = "recruitment_degree")
    private String recruitmentDegree;

    // yêu cầu kinh nghiệm
    @Column(name = "recruitment_experience")
    private String recruitmentExperience;

    @Column(name = "salary")
    private Double salary;

    @Column(name = "level")
    private String level;

    @Column(name = "working_form")
    private String workingForm;

    @Column(name = "job_description")
    private String jobDescription;

    @Column(name = "job_requirements")
    private String jobRequirements;

    @Column(name = "benefits")
    private String benefits;
}
