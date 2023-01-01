package com.hau.huylong.graduation_proejct.model.dto.hau;

import lombok.Data;

import java.util.Date;

@Data
public class RecruitmentProfileDTO {
    private Long id;
    private String createdBy;
    private Date created;
    private String updatedBy;
    private Date updated;
    // vị trí ứng tuyển
    private String positionOffer;
    //cấp bậc mong muốn
    private String levelDesire;
    //trình độ học vấn
    private String academyLevel;
    // ngành nghề
    private String career;
    //hình thức làm việc
    private String workForm;
    // cấp bậc hiện tại
    private String currentLevel;
    // mức lương mong muốn
    private Float offerSalary;
    // số năm kinh nghiệm
    private Float experienceNumber;
    // Nơi làm việc
    private String workAddress;
    // Thông tin người dùng
    private Long userId;
    // Địa chỉ
    private String address;
    // mục tiêu nghề nghiệp
    private String careerTarget;
    // kỹ năng mềm
    private String sortSkill;
    // Kinh nghiệm làm việc
    private WorkExperienceDTO workExperienceDTO;
    // Thông tin học vấn
    private AcademyInfoDTO academyInfoDTO;
    // Ngoại ngữ
    private ForeignLanguageDTO foreignLanguageDTO;
    // Tin học
    private OfficeInfoDTO officeInfoDTO;
}
