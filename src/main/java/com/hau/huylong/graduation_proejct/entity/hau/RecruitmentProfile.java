package com.hau.huylong.graduation_proejct.entity.hau;

import com.hau.huylong.graduation_proejct.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "recruitment_profile")
@Data
public class RecruitmentProfile extends BaseEntity {
    @Column(name = "file_id")
    private String fileId;
    @Column(name = "position_offer")
    private String positionOffer;
    //cấp bậc mong muốn
    @Column(name = "level_desire")
    private String levelDesire;
    //trình độ học vấn
    @Column(name = "academy_level")
    private String academyLevel;
    // ngành nghề
    @Column(name = "career")
    private String career;
    //hình thức làm việc
    @Column(name = "work_form")
    private String workForm;
    // cấp bậc hiện tại
    @Column(name = "current_level")
    private String currentLevel;
    // mức lương mong muốn
    @Column(name = "offer_salary")
    private Float offerSalary;
    // số năm kinh nghiệm
    @Column(name = "experience_number")
    private Float experienceNumber;
    // Nơi làm việc
    @Column(name = "work_address")
    private String workAddress;
    // Thông tin người dùng
    @Column(name = "user_id")
    private Long userId;
    // Địa chỉ
    @Column(name = "address")
    private String address;
    // mục tiêu nghề nghiệp
    @Column(name = "career_target")
    private String careerTarget;
    @Column(name = "sort_skill")
    private String sortSkill;
    // Kinh nghiệm làm việc
    @Column(name = "work_experience")
    @Lob
    private String workExperience;
    // Thông tin học vấn
    @Column(name = "academy_info")
    @Lob
    private String academyInfo;
    // Ngoại ngữ
    @Column(name = "foreign_language")
    @Lob
    private String foreignLanguage;
    // Tin học
    @Column(name = "office_info")
    @Lob
    private String officeInfo;

    @Column(name = "permission_search", columnDefinition = "boolean default false")
    private Boolean permissionSearch;

    @Column(name = "view")
    private Integer view;

    @Column(name = "time_submit")
    private Date timeSubmit;
}
