package com.hau.huylong.graduation_proejct.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hau.huylong.graduation_proejct.common.exception.APIException;
import com.hau.huylong.graduation_proejct.common.util.BeanUtil;
import com.hau.huylong.graduation_proejct.common.util.PageableUtils;
import com.hau.huylong.graduation_proejct.entity.auth.CustomUser;
import com.hau.huylong.graduation_proejct.entity.hau.RecruitmentProfile;
import com.hau.huylong.graduation_proejct.entity.hau.UserInfo;
import com.hau.huylong.graduation_proejct.model.dto.hau.*;
import com.hau.huylong.graduation_proejct.model.request.SearchRecruitmentProfileRequest;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.repository.auth.UserInfoReps;
import com.hau.huylong.graduation_proejct.repository.hau.RecruitmentProfileReps;
import com.hau.huylong.graduation_proejct.service.GoogleDriverFile;
import com.hau.huylong.graduation_proejct.service.RecruitmentProfileService;
import com.hau.huylong.graduation_proejct.service.mapper.RecruitmentProfileMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class RecruitmentProfileServiceImpl implements RecruitmentProfileService {
    private final RecruitmentProfileMapper recruitmentProfileMapper;
    private final RecruitmentProfileReps recruitmentProfileReps;
    private final GoogleDriverFile googleDriverFile;
    private final UserInfoReps userInfoReps;

    @Override
    public RecruitmentProfileDTO save(RecruitmentProfileDTO recruitmentProfileDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        ObjectMapper mapper = new ObjectMapper();
        recruitmentProfileDTO.setUserId(customUser.getId().longValue());
        RecruitmentProfile recruitmentProfile = recruitmentProfileMapper.from(recruitmentProfileDTO);

        return getRecruitmentProfileDTO(recruitmentProfileDTO, mapper, recruitmentProfile);
    }

    @Override
    public RecruitmentProfileDTO edit(Long id, RecruitmentProfileDTO recruitmentProfileDTO) {
        ObjectMapper mapper = new ObjectMapper();
        Optional<RecruitmentProfile> recruitmentProfileOptional = recruitmentProfileReps.findById(id);

        if (recruitmentProfileOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy hồ sơ tuyển dụng");
        }

        RecruitmentProfile recruitmentProfile = recruitmentProfileOptional.get();
        BeanUtil.copyNonNullProperties(recruitmentProfileDTO, recruitmentProfile);

        return getRecruitmentProfileDTO(recruitmentProfileDTO, mapper, recruitmentProfile);
    }

    private RecruitmentProfileDTO getRecruitmentProfileDTO(RecruitmentProfileDTO recruitmentProfileDTO,
                                                           ObjectMapper mapper, RecruitmentProfile recruitmentProfile) {
        mapper.registerModule(new JavaTimeModule());
        try {
            if (recruitmentProfileDTO.getAcademyInfoDTO() != null) {
                recruitmentProfile.setAcademyInfo(mapper.writeValueAsString(recruitmentProfileDTO.getAcademyInfoDTO()));
            }

            if (recruitmentProfileDTO.getWorkExperienceDTO() != null) {
                recruitmentProfile.setWorkExperience(mapper.writeValueAsString(recruitmentProfileDTO.getWorkExperienceDTO()));
            }

            if (recruitmentProfileDTO.getForeignLanguageDTO() != null) {
                recruitmentProfile.setForeignLanguage(mapper.writeValueAsString(recruitmentProfileDTO.getForeignLanguageDTO()));
            }

            if (recruitmentProfileDTO.getOfficeInfoDTO() != null) {
                recruitmentProfile.setOfficeInfo(mapper.writeValueAsString(recruitmentProfileDTO.getOfficeInfoDTO()));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return recruitmentProfileMapper.to(recruitmentProfileReps.save(recruitmentProfile));
    }

    @Override
    public void delete(Long id) {
        Optional<RecruitmentProfile> recruitmentProfileOptional = recruitmentProfileReps.findById(id);

        if (recruitmentProfileOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy hồ sơ tuyển dụng");
        }

        recruitmentProfileReps.delete(recruitmentProfileOptional.get());
    }

    @Override
    public RecruitmentProfileDTO findById(Long id) {
        ObjectMapper objectMapper = new ObjectMapper();
        Optional<RecruitmentProfile> recruitmentProfileOptional = recruitmentProfileReps.findById(id);

        if (recruitmentProfileOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy hồ sơ tuyển dụng");
        }

        RecruitmentProfileDTO recruitmentProfileDTO = recruitmentProfileMapper.to(recruitmentProfileOptional.get());

        setDTOProfile(objectMapper, recruitmentProfileDTO);

        return recruitmentProfileDTO;
    }

    @Override
    public PageDataResponse<RecruitmentProfileDTO> getAll(SearchRecruitmentProfileRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        Pageable pageable = PageableUtils.of(request.getPage(), request.getSize());
        Page<RecruitmentProfileDTO> page = recruitmentProfileReps.search(request, pageable).map(recruitmentProfileMapper::to);

        if (!page.isEmpty()) {
            page.forEach(p -> setDTOProfile(objectMapper, p));
        }

        return PageDataResponse.of(page);
    }

    private void setDTOProfile(ObjectMapper objectMapper, RecruitmentProfileDTO p) {
        objectMapper.registerModule(new JavaTimeModule());
        try {
            AcademyInfoDTO academyInfoDTO = objectMapper
                    .readValue(p.getAcademyInfo(), AcademyInfoDTO.class);
            WorkExperienceDTO workExperienceDTO = objectMapper
                    .readValue(p.getWorkExperience(), WorkExperienceDTO.class);
            ForeignLanguageDTO foreignLanguageDTO = objectMapper
                    .readValue(p.getForeignLanguage(), ForeignLanguageDTO.class);
            OfficeInfoDTO officeInfoDTO = objectMapper
                    .readValue(p.getOfficeInfo(), OfficeInfoDTO.class);

            p.setAcademyInfoDTO(academyInfoDTO);
            p.setForeignLanguageDTO(foreignLanguageDTO);
            p.setWorkExperienceDTO(workExperienceDTO);
            p.setOfficeInfoDTO(officeInfoDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RecruitmentProfileDTO findByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        ObjectMapper objectMapper = new ObjectMapper();
        Optional<RecruitmentProfile> recruitmentProfileOptional = recruitmentProfileReps.findByUserId(customUser.getId().longValue());

        if (recruitmentProfileOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy hồ sơ tuyển dụng");
        }

        RecruitmentProfileDTO recruitmentProfileDTO = recruitmentProfileMapper.to(recruitmentProfileOptional.get());

        setDTOProfile(objectMapper, recruitmentProfileDTO);

        return recruitmentProfileDTO;
    }

    @Override
    public void activeSearch(boolean check) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        Optional<RecruitmentProfile> recruitmentProfileOptional = recruitmentProfileReps.findByUserId(customUser.getId().longValue());

        if (recruitmentProfileOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy hồ sơ tuyển dụng");
        }

        if (check) {
            recruitmentProfileOptional.get().setPermissionSearch(true);
        } else {
            recruitmentProfileOptional.get().setPermissionSearch(false);
        }

        recruitmentProfileReps.save(recruitmentProfileOptional.get());
    }

    @Override
    public void uploadProfile(MultipartFile file, String filePath, boolean isPublic) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        String fileId = googleDriverFile.uploadFile(file, filePath, isPublic);
        if (fileId != null) {
            Optional<RecruitmentProfile> recruitmentProfileOptional = recruitmentProfileReps.findByUserId(customUser.getId().longValue());

            if (recruitmentProfileOptional.isEmpty()) {
                throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy hồ sơ tuyển dụng");
            }

            RecruitmentProfile recruitmentProfile = recruitmentProfileOptional.get();
            recruitmentProfile.setFileId(fileId);

            recruitmentProfileReps.save(recruitmentProfile);
        }
    }

    @Override
    public void saveListProfile(List<Long> ids) {
        ObjectMapper objectMapper = new ObjectMapper();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        Optional<UserInfo> userOptional = userInfoReps.findByUserId(customUser.getId());

        if (userOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không thể tìm thấy người dùng!");
        }

        if (ids != null && !ids.isEmpty()) {
            try {
                userOptional.get().setArrRecruitmentIds(objectMapper.writeValueAsString(ids));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        userInfoReps.save(userOptional.get());
    }

    @Override
    public List<RecruitmentProfileDTO> getByListProfileId(List<Long> ids) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (ids != null && !ids.isEmpty()) {
            List<RecruitmentProfile> recruitmentProfiles = recruitmentProfileReps.findByIdIn(ids);

            if (recruitmentProfiles.isEmpty()) {
                throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy hồ sơ tuyển dụng");
            }

            List<RecruitmentProfileDTO> recruitmentProfileDTOS = recruitmentProfiles.stream()
                    .map(recruitmentProfileMapper::to).collect(Collectors.toList());

            if (!recruitmentProfileDTOS.isEmpty()) {
                recruitmentProfileDTOS.forEach(r -> setDTOProfile(objectMapper, r));
            }

            return recruitmentProfileDTOS;
        }
        return null;
    }
}
