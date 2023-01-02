package com.hau.huylong.graduation_proejct.service;

import com.hau.huylong.graduation_proejct.model.dto.hau.RecruitmentProfileDTO;
import com.hau.huylong.graduation_proejct.model.request.SearchRecruitmentProfileRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RecruitmentProfileService extends GenericService<RecruitmentProfileDTO, SearchRecruitmentProfileRequest> {
    RecruitmentProfileDTO findByUserId();
    void activeSearch(boolean check);
    void uploadProfile(MultipartFile file, String filePath, boolean isPublic);
    void saveListProfile(List<Long> ids);
    List<RecruitmentProfileDTO> getByListProfileId(List<Long> ids);
}
