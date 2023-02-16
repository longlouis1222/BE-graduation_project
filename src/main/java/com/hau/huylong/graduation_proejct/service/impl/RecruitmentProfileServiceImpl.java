package com.hau.huylong.graduation_proejct.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hau.huylong.graduation_proejct.common.exception.APIException;
import com.hau.huylong.graduation_proejct.common.util.BeanUtil;
import com.hau.huylong.graduation_proejct.common.util.PageableUtils;
import com.hau.huylong.graduation_proejct.common.util.StringUtils;
import com.hau.huylong.graduation_proejct.entity.auth.CustomUser;
import com.hau.huylong.graduation_proejct.entity.hau.RecruitmentProfile;
import com.hau.huylong.graduation_proejct.entity.hau.UserInfo;
import com.hau.huylong.graduation_proejct.model.dto.auth.UserDTO;
import com.hau.huylong.graduation_proejct.model.dto.auth.UserInfoDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.*;
import com.hau.huylong.graduation_proejct.model.request.SearchRecruitmentProfileRequest;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.repository.auth.UserInfoReps;
import com.hau.huylong.graduation_proejct.repository.auth.UserReps;
import com.hau.huylong.graduation_proejct.repository.hau.RecruitmentProfileReps;
import com.hau.huylong.graduation_proejct.service.GoogleDriverFile;
import com.hau.huylong.graduation_proejct.service.RecruitmentProfileService;
import com.hau.huylong.graduation_proejct.service.mapper.RecruitmentProfileMapper;
import com.hau.huylong.graduation_proejct.service.mapper.UserInfoMapper;
import com.hau.huylong.graduation_proejct.service.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class RecruitmentProfileServiceImpl implements RecruitmentProfileService {
    private final RecruitmentProfileMapper recruitmentProfileMapper;
    private final RecruitmentProfileReps recruitmentProfileReps;
    private final GoogleDriverFile googleDriverFile;
    private final UserInfoReps userInfoReps;
    private final UserInfoMapper userInfoMapper;
    private final UserReps userReps;
    private final UserMapper userMapper;

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

        return getRecruitmentProfileDTO(objectMapper, recruitmentProfileOptional);
    }

    private RecruitmentProfileDTO getRecruitmentProfileDTO(ObjectMapper objectMapper, Optional<RecruitmentProfile> recruitmentProfileOptional) {
        RecruitmentProfileDTO recruitmentProfileDTO = recruitmentProfileMapper.to(recruitmentProfileOptional.get());

        setDTOProfile(objectMapper, recruitmentProfileDTO);
        Map<Integer, UserDTO> mapUser = getUser(Collections.singletonList(recruitmentProfileDTO.getUserId()));
        if (!mapUser.isEmpty() && mapUser.containsKey(recruitmentProfileDTO.getUserId().intValue())) {
            recruitmentProfileDTO.setUserDTO(mapUser.get(recruitmentProfileDTO.getUserId().intValue()));
        }

        return recruitmentProfileDTO;
    }

    @Override
    public PageDataResponse<RecruitmentProfileDTO> getAll(SearchRecruitmentProfileRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        Pageable pageable = PageableUtils.of(request.getPage(), request.getSize());
        Page<RecruitmentProfileDTO> page = recruitmentProfileReps.search(request, pageable).map(recruitmentProfileMapper::to);

        if (!page.isEmpty()) {
            List<Long> userIds = page.map(RecruitmentProfileDTO::getUserId).toList();
            Map<Integer, UserDTO> mapUser = getUser(userIds);
            page.forEach(p -> {
                setDTOProfile(objectMapper, p);
                if (!mapUser.isEmpty() && mapUser.containsKey(p.getUserId().intValue())) {
                    p.setUserDTO(mapUser.get(p.getUserId().intValue()));
                }
            });
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
        Optional<RecruitmentProfile> recruitmentProfileOptional = recruitmentProfileReps
                .findByUserId(customUser.getId().longValue());

        if (recruitmentProfileOptional.isPresent()) {
            return getRecruitmentProfileDTO(objectMapper, recruitmentProfileOptional);
        }

        return null;
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
    public String uploadProfile(MultipartFile file, String filePath, boolean isPublic) {
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

        return fileId;
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

        if (ids != null) {
            try {
                userOptional.get().setArrRecruitmentIds(objectMapper.writeValueAsString(ids));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        if (!CollectionUtils.isEmpty(ids)) {
            List<RecruitmentProfile> recruitmentProfilesUpdate = new ArrayList<>();
            List<RecruitmentProfile> recruitmentProfiles = recruitmentProfileReps.findByIdIn(ids);
            if (!CollectionUtils.isEmpty(recruitmentProfiles)) {
                recruitmentProfiles.forEach(r -> {
                    r.setTimeSubmit(Date.from(Instant.now()));
                    recruitmentProfilesUpdate.add(r);
                });
            }

            recruitmentProfileReps.saveAll(recruitmentProfilesUpdate);
        }

        userInfoReps.save(userOptional.get());
    }

    public void removeProfileRecruitment(Long profileId) {
        ObjectMapper objectMapper = new ObjectMapper();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        Optional<UserInfo> userOptional = userInfoReps.findByUserId(customUser.getId());

        if (userOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không thể tìm thấy người dùng!");
        }

        List<Long> profileIds = new ArrayList<>();
        if (!StringUtils.isNullOrEmpty(userOptional.get().getArrRecruitmentIds())) {
            try {
                List<Integer> profileRecruitmentIds = objectMapper.readValue(userOptional.get().getArrRecruitmentIds(), List.class);
                if (!CollectionUtils.isEmpty(profileRecruitmentIds)) {
                    profileRecruitmentIds.forEach(i -> profileIds.add(Long.parseLong(String.valueOf(i))));
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        if (!CollectionUtils.isEmpty(profileIds)) {
            profileIds.remove(profileId);

            try {
                String profileIdString = objectMapper.writeValueAsString(profileIds);
                userOptional.get().setArrRecruitmentIds(profileIdString);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            userInfoReps.save(userOptional.get());
        }
    }

    @Override
    public PageDataResponse<RecruitmentProfileDTO> getByListProfileId(SearchRecruitmentProfileRequest request) {
        Pageable pageable = PageableUtils.of(request.getPage(), request.getSize());
        Page<RecruitmentProfileDTO> recruitmentProfileDTOS = null;
        ObjectMapper objectMapper = new ObjectMapper();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        Optional<UserInfo> userOptional = userInfoReps.findByUserId(customUser.getId());

        if (userOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không thể tìm thấy người dùng!");
        }

        List<Long> profileIds = new ArrayList<>();
        try {
            if (!StringUtils.isNullOrEmpty(userOptional.get().getArrRecruitmentIds())) {
                List<Integer> list = objectMapper.readValue(userOptional.get().getArrRecruitmentIds(), List.class);
                if (!CollectionUtils.isEmpty(list)) {
                    list.forEach(i -> profileIds.add(Long.parseLong(String.valueOf(i))));
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        if (!CollectionUtils.isEmpty(profileIds)) {
            recruitmentProfileDTOS = recruitmentProfileReps.getListProfile(request, profileIds, pageable).map(recruitmentProfileMapper::to);

            if (!recruitmentProfileDTOS.isEmpty()) {
                List<Long> userIds = recruitmentProfileDTOS.stream().map(RecruitmentProfileDTO::getUserId).collect(Collectors.toList());
                Map<Integer, UserDTO> mapUser = getUser(userIds);

                recruitmentProfileDTOS.forEach(r -> {
                    setDTOProfile(objectMapper, r);

                    if (!CollectionUtils.isEmpty(mapUser) && mapUser.containsKey(r.getUserId().intValue())) {
                        r.setUserDTO(mapUser.get(r.getUserId().intValue()));
                    }
                });
            }
        }

        return PageDataResponse.of(recruitmentProfileDTOS);
    }

    @Override
    public Integer viewProfile(Long profileId) {
        Optional<RecruitmentProfile> recruitmentProfileOptional = recruitmentProfileReps.findById(profileId);

        if (recruitmentProfileOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy hồ sơ tuyển dụng");
        }

        if (recruitmentProfileOptional.get().getView() == null) {
            recruitmentProfileOptional.get().setView(0);
        }

        recruitmentProfileOptional.get().setView(recruitmentProfileOptional.get().getView() + 1);
        recruitmentProfileReps.save(recruitmentProfileOptional.get());

        return recruitmentProfileOptional.get().getView();
    }

    private Map<Integer, UserDTO> getUser(List<Long> userIds) {
        Map<Integer, UserDTO> map = new HashMap<>();

        if (userIds != null && !userIds.isEmpty()) {
            List<Integer> userIdInts = new ArrayList<>();
            userIds.forEach(u -> userIdInts.add(Integer.parseInt(String.valueOf(u))));
            map = userReps.findByIds(userIdInts).stream().map(userMapper::to).collect(Collectors.toMap(UserDTO::getId, u -> u));
            Map<Integer, UserInfoDTO> mapUserInfo = getUseInfo(userIdInts);
            if (!map.isEmpty()) {
                map.forEach((k, v) -> {
                    if (!mapUserInfo.isEmpty() && mapUserInfo.containsKey(k)) {
                        v.setUserInfoDTO(mapUserInfo.get(k));
                    }
                });
            }
        }

        return map;
    }

    private Map<Integer, UserInfoDTO> getUseInfo(List<Integer> userIds) {
        Map<Integer, UserInfoDTO> map = new HashMap<>();
        if (userIds != null && !userIds.isEmpty()) {
             map = userInfoReps.findByUserIdIn(userIds).stream().map(userInfoMapper::to)
                     .collect(Collectors.toMap(UserInfoDTO::getUserId, ui -> ui));
        }
        return map;
    }
}
