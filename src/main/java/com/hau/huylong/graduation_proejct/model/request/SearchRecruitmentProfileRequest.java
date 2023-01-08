package com.hau.huylong.graduation_proejct.model.request;

import com.hau.huylong.graduation_proejct.model.dto.hau.AcademyInfoDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.ForeignLanguageDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.OfficeInfoDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.WorkExperienceDTO;
import lombok.Data;

@Data
public class SearchRecruitmentProfileRequest extends SearchRequest {
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
}
