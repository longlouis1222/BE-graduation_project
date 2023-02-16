package com.hau.huylong.graduation_proejct.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hau.huylong.graduation_proejct.common.enums.TypeUser;
import com.hau.huylong.graduation_proejct.common.exception.APIException;
import com.hau.huylong.graduation_proejct.common.util.MapUtil;
import com.hau.huylong.graduation_proejct.common.util.PageableUtils;
import com.hau.huylong.graduation_proejct.common.util.StringUtils;
import com.hau.huylong.graduation_proejct.entity.auth.CustomUser;
import com.hau.huylong.graduation_proejct.entity.auth.Role;
import com.hau.huylong.graduation_proejct.entity.auth.User;
import com.hau.huylong.graduation_proejct.entity.hau.*;
import com.hau.huylong.graduation_proejct.model.dto.auth.UserDTO;
import com.hau.huylong.graduation_proejct.model.dto.auth.UserInfoDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.*;
import com.hau.huylong.graduation_proejct.model.request.SearchPostRequest;
import com.hau.huylong.graduation_proejct.model.request.SearchRecruitmentProfileRequest;
import com.hau.huylong.graduation_proejct.model.request.UserRequest;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.repository.auth.RoleReps;
import com.hau.huylong.graduation_proejct.repository.auth.UserInfoReps;
import com.hau.huylong.graduation_proejct.repository.auth.UserReps;
import com.hau.huylong.graduation_proejct.repository.hau.*;
import com.hau.huylong.graduation_proejct.service.GoogleDriverFile;
import com.hau.huylong.graduation_proejct.service.UserService;
import com.hau.huylong.graduation_proejct.service.mapper.*;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserReps userReps;
    private final RoleReps roleReps;
    private final UserInfoReps userInfoReps;
    private final UserInfoMapper userInfoMapper;
    private final CompanyReps companyReps;
    private final CompanyMapper companyMapper;
    private final GoogleDriverFile googleDriverFile;
    private final UserRecruitmentPostReps userRecruitmentPostReps;
    private final PostReps postReps;
    private final PostMapper postMapper;
    private final IndustryMapper industryMapper;
    private final IndustryReps industryReps;
    private final RecruitmentProfileReps recruitmentProfileReps;
    private final RecruitmentProfileMapper recruitmentProfileMapper;

    /**
     * Tìm kiếm người dùng theo username và status
     *
     * @Param: username
     * @Param: status
     * @Return: User
     */
    @Override
    @Cacheable("users")
    public Optional<User> findByUsernameAndStatus(String username, short status) {
        log.info("calling findByUsernameAndStatus");
        return userReps.findByUsernameAndStatus(username, status);
    }

    /**
     * Danh sách người dùng
     *
     * @Param: request
     * @Return: PageDataResponse<UserDTO>
     */
    @Override
    public PageDataResponse<UserDTO> getAll(UserRequest request) {
        if (request.getType() != null) {
            if (request.getType().equalsIgnoreCase("employer")) {
                request.setTypeUser(TypeUser.EMPLOYER);
            } else {
                request.setTypeUser(TypeUser.CANDIDATE);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Pageable pageable = PageableUtils.of(request.getPage(), request.getSize());

        Page<UserDTO> pageData = userReps.search(request, pageable).map(userMapper::to);

        if (!pageData.isEmpty()) {
            List<Integer> userIds = pageData.map(UserDTO::getId).toList();
            Map<Integer, UserInfoDTO> mapUserInfo = mapUserInfo(userIds, objectMapper);

            Map<Integer, CompanyDTO> mapCompanyDTO = companyReps.findByUserIdIn(userIds)
                    .stream().map(companyMapper::to).collect(Collectors.toMap(CompanyDTO::getUserId, c -> c));

            pageData.forEach(p -> {
                if (!mapUserInfo.isEmpty() && mapUserInfo.containsKey(p.getId())) {
                    p.setUserInfoDTO(mapUserInfo.get(p.getId()));
                }

                if (!mapCompanyDTO.isEmpty() && mapCompanyDTO.containsKey(p.getId())) {
                    p.setCompanyDTO(mapCompanyDTO.get(p.getId()));
                }
            });
        }

        return PageDataResponse.of(pageData);
    }

    private Map<Integer, UserInfoDTO> mapUserInfo(List<Integer> userIds, ObjectMapper objectMapper) {
        Map<Integer, UserInfoDTO> mapUserInfo = new HashMap<>();
        if (!CollectionUtils.isEmpty(userIds)) {
             mapUserInfo = userInfoReps.findByUserIdIn(userIds)
                .stream().map(u -> {
                    UserInfoDTO userInfoDTO = userInfoMapper.to(u);
                    if (!StringUtils.isNullOrEmpty(userInfoDTO.getArrRecruitmentIds())) {
                        try {
                            List<Integer> list = objectMapper.readValue(userInfoDTO.getArrRecruitmentIds(), List.class);
                            if (!CollectionUtils.isEmpty(list)) {
                                List<Long> arr = new ArrayList<>();
                                list.forEach(i -> arr.add(Long.parseLong(String.valueOf(i))));
                                userInfoDTO.setArrProfileIds(arr);
                            }
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return userInfoDTO;
                }).collect(Collectors.toMap(UserInfoDTO::getUserId, ui -> ui));
        }
        return mapUserInfo;
    }

    /**
     * Chỉnh sửa người dùng
     *
     * @Param: userId
     * @RequestBody: UserDTO
     * @Return: UserDTO
     */
    @Override
    public UserDTO edit(Integer userId, UserRequest userRequest) {
        Optional<User> userOptional = userReps.findById(userId);

        if (userOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không thể tìm thấy người dùng với id  " + userOptional);
        }

        // update account and roles to user
        User user = userOptional.get();
        user.setUsername(userRequest.getUsername());

        if (userRequest.getListRole() != null) {
            List<Role> roles = roleReps.findByCodes(userRequest.getListRole());
            Set<Role> roleSet = new HashSet<>(roles);
            user.setRoles(roleSet);
        }

        //update user info or company info
        UserInfo userInfo = userInfoReps.findByUserId(user.getId())
                .orElseThrow(() -> APIException.from(HttpStatus.NOT_FOUND).withMessage("User not found."));

        CompanyDTO companyDto = null;
        UserInfoDTO userInfoDTO = null;

        if (userRequest.getType() != null) {
            if (userRequest.getType().equalsIgnoreCase("employer")) {
                Company company = companyReps.findByUserId(user.getId())
                        .orElseThrow(() -> APIException.from(HttpStatus.NOT_FOUND).withMessage("Company not found."));
                UserInfo userContactInfo = userInfoReps.findByCompanyId(company.getId())
                        .orElseThrow(() -> APIException.from(HttpStatus.NOT_FOUND).withMessage("User not found."));

                // set person contact info
                userContactInfo.setFullName(userRequest.getUserInfoRequest().getFullName());
                userContactInfo.setPhoneNumber(userRequest.getUserInfoRequest().getPhoneNumber());
                userContactInfo.setAddress(userRequest.getUserInfoRequest().getAddress());

                // set company info
                company.setCompanyAddress(userRequest.getCompanyRequest().getCompanyAddress());
                company.setName(userRequest.getCompanyRequest().getName());
                company.setCompanyPhoneNumber(userRequest.getCompanyRequest().getCompanyPhoneNumber());
                company.setTaxCode(userRequest.getCompanyRequest().getTaxCode());
                company.setEmployeeNumber(userRequest.getCompanyRequest().getEmployeeNumber());
                company.setFieldOfActivity(userRequest.getCompanyRequest().getFieldOfActivity());
                company.setLocation(userRequest.getCompanyRequest().getLocation());
                company.setEmailCompany(userRequest.getCompanyRequest().getEmailCompany());
                company.setBusinessIntroduction(userRequest.getCompanyRequest().getBusinessIntroduction());
                company.setBusinessLicense(userRequest.getCompanyRequest().getBusinessLicense());
                user.setType(TypeUser.EMPLOYER);

                userInfoDTO = userInfoMapper.to(userInfoReps.save(userContactInfo));
                companyDto = companyMapper.to(companyReps.save(company));
            } else {
                userInfo.setGender(userRequest.getUserInfoRequest().getGender());
                userInfo.setAddress(userRequest.getUserInfoRequest().getAddress());
                userInfo.setAvatar(userRequest.getUserInfoRequest().getAvatar());
                userInfo.setTown(userRequest.getUserInfoRequest().getTown());
                userInfo.setDateOfBirth(userRequest.getUserInfoRequest().getDateOfBirth());
                userInfo.setFullName(userRequest.getUserInfoRequest().getFullName());
                userInfo.setMarriageStatus(userRequest.getUserInfoRequest().getMarriageStatus());
                userInfo.setPhoneNumber(userRequest.getUserInfoRequest().getPhoneNumber());
                user.setType(TypeUser.CANDIDATE);

                userInfoDTO = userInfoMapper.to(userInfoReps.save(userInfo));
            }
        }

        UserDTO userDTO = userMapper.to(userReps.save(user));

        if (userInfoDTO != null && Objects.equals(userInfoDTO.getUserId(), userDTO.getId())) {
            userDTO.setUserInfoDTO(userInfoDTO);
        } else if (companyDto != null && Objects.equals(companyDto.getUserId(), userDTO.getId())) {
            userDTO.setCompanyDTO(companyDto);
            if (userInfoDTO != null && Objects.equals(companyDto.getId(), userInfoDTO.getCompanyId())) {
                userDTO.setUserInfoDTO(userInfoDTO);
            }
        }

        return userDTO;
    }


    @Override
    public void addRoleToUser(List<Integer> roleIds, List<Integer> userIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>(roleReps.findByIdIn(roleIds));
            if (userIds != null && !userIds.isEmpty()) {
                List<User> users = userReps.findByIds(userIds);

                if (!users.isEmpty() && !roleIds.isEmpty()) {
                    users.forEach(u -> u.setRoles(roles));
                    userReps.saveAll(users);
                }
            }
        }
    }

    @Override
    public void deleteUser(List<Integer> userIds) {
        if (userIds != null && !userIds.isEmpty()) {
            List<User> users = userReps.findByIds(userIds);
            List<UserInfo> userInfos = userInfoReps.findByUserIdIn(userIds);
            if (!users.isEmpty()) {
                userReps.deleteAll(users);
            }

            if (!userInfos.isEmpty()) {
                userInfoReps.deleteAll(userInfos);
            }
        }
    }

    @Override
    public List<UserDTO> findById(List<Integer> userIds) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (userIds != null && !userIds.isEmpty()) {
            List<UserDTO> users = userReps.findByIds(userIds)
                    .stream().map(userMapper::to).collect(Collectors.toList());
            Map<Integer, UserInfoDTO> userInfos = mapUserInfo(userIds, objectMapper);
            Map<Integer, CompanyDTO> mapCompanyDTO = companyReps.findByUserIdIn(userIds)
                    .stream().map(companyMapper::to).collect(Collectors.toMap(CompanyDTO::getUserId, c -> c));

            if (!users.isEmpty()) {
                users.forEach(u -> {
                    if (!userInfos.isEmpty() && userInfos.containsKey(u.getId())) {
                        u.setUserInfoDTO(userInfos.get(u.getId()));
                    }

                    if (!mapCompanyDTO.isEmpty() && mapCompanyDTO.containsKey(u.getId())) {
                        u.setCompanyDTO(mapCompanyDTO.get(u.getId()));
                    }
                });
            }

            return users;
        }
        return null;
    }

    @Override
    public void uploadAvatar(MultipartFile file, String filePath, boolean isPublic) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();
        String fileId = googleDriverFile.uploadFile(file, filePath, isPublic);
        if (fileId != null) {
            Optional<UserInfo> userInfoOptional = userInfoReps.findByUserId(user.getId());

            if (userInfoOptional.isEmpty()) {
                throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy người dùng");
            }

            UserInfo userInfo = userInfoOptional.get();
            userInfo.setAvatar(fileId);

            userInfoReps.save(userInfo);
        }
    }

    @Override
    public void inActive(Integer userId, boolean check) {
        Optional<User> userOptional = userReps.findById(userId);

        if (userOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy người dùng với id  " + userOptional);
        }

        if (check) {
            userOptional.get().setStatus(User.Status.ACTIVE);
        } else {
            userOptional.get().setStatus(User.Status.LOCK);
        }

        userReps.save(userOptional.get());
    }

    @Override
    public String uploadCompanyProfile(MultipartFile file, String filePath, boolean isPublic) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();

        Optional<Company> companyOptional = companyReps.findByUserId(user.getId());
        if (companyOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy công ty");
        }

        String fileId = googleDriverFile.uploadFile(file, filePath, isPublic);
        companyOptional.get().setFileId(fileId);
        companyReps.save(companyOptional.get());
        return fileId;
    }

    @Override
    public void userSubmitPostRecruitment(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();

        Optional<Post> postOptional = postReps.findById(postId);
        if (postOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy bài viết");
        }

        UserRecruitmentPost userPost = new UserRecruitmentPost();
        userPost.setPostId(postOptional.get().getId());
        userPost.setCompanyId(postOptional.get().getCompanyId());
        userPost.setUserId(user.getId());
        userRecruitmentPostReps.save(userPost);
    }

    @Override
    public void removePostByCurrentUserSubmit(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();
        List<UserRecruitmentPost> userPosts = userRecruitmentPostReps.findByUserIdAndPostId(user.getId(), postId);

        if (!CollectionUtils.isEmpty(userPosts)) {
            userRecruitmentPostReps.deleteAll(userPosts);
        }
    }

    @Override
    public PageDataResponse<PostDTO> getAllPostUserRecruitment(SearchPostRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();

        List<UserRecruitmentPost> userPosts = userRecruitmentPostReps.findByUserId(user.getId());
        List<Long> postIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userPosts)) {
            postIds = userPosts.stream().map(UserRecruitmentPost::getPostId).distinct().collect(Collectors.toList());
        }

        Pageable pageable = PageableUtils.of(request.getPage(), request.getSize());
        Page<PostDTO> page = userRecruitmentPostReps.search(request, postIds, pageable).map(postMapper::to);

        if (!page.isEmpty()) {
            List<Long> companyIds = page.stream().map(PostDTO::getCompanyId).collect(Collectors.toList());
            List<Long> industryIds = page.stream().map(PostDTO::getIndustryId).collect(Collectors.toList());
            Map<Long, CompanyDTO> companyDTOMap = setCompanyDTO(companyIds);
            Map<Long, IndustryDTO> industryDTOMap = setIndustryDTO(industryIds);

            page.forEach(p -> {
                if (companyDTOMap.containsKey(p.getCompanyId())) {
                    p.setCompanyDTO(companyDTOMap.get(p.getCompanyId()));
                }

                if (industryDTOMap.containsKey(p.getIndustryId())) {
                    p.setIndustryDTO(industryDTOMap.get(p.getIndustryId()));
                }
            });
        }

        return PageDataResponse.of(page);
    }

    @Override
    public PageDataResponse<RecruitmentProfileDTO> getAllPostUserRecruitmentOfEmployee(SearchRecruitmentProfileRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();

        Page<RecruitmentProfileDTO> page = null;
        List<Long> userIdLongs = new ArrayList<>();
        if (user.getType().equalsIgnoreCase(TypeUser.EMPLOYER.name())) {
            List<Company> companies = companyReps.findByUserIdIn(Collections.singletonList(user.getId()));
            if (!CollectionUtils.isEmpty(companies)) {
                List<Long> companyIds = companies.stream().map(Company::getId).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(companyIds)) {
                    List<UserRecruitmentPost> userRecruitmentPosts = userRecruitmentPostReps.findByCompanyIdIn(companyIds);
                    userIdLongs = userRecruitmentPosts.stream().map(u -> Long.parseLong(String.valueOf(u.getUserId()))).distinct().collect(Collectors.toList());
                }
            }

            Pageable pageable = PageableUtils.of(request.getPage(), request.getSize());
            page = recruitmentProfileReps.getListProfileOfCandidate(request, userIdLongs, pageable).map(recruitmentProfileMapper::to);

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

        } else {
            throw APIException.from(HttpStatus.BAD_REQUEST).withMessage(user.getFullName() + " Không phải là nhà tuyển dụng");
        }

        return PageDataResponse.of(page);
    }

    @Override
    public void removeRecruitmentByEmployee(Long profileId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();

        if (user.getType().equalsIgnoreCase(TypeUser.EMPLOYER.name())) {
            List<Company> companies = companyReps.findByUserIdIn(Collections.singletonList(user.getId()));
            List<Long> companyIds = companies.stream().map(Company::getId).collect(Collectors.toList());
            List<RecruitmentProfile> recruitmentProfiles = recruitmentProfileReps.findByIdIn(Collections.singletonList(profileId));

            if (!CollectionUtils.isEmpty(recruitmentProfiles)) {
                List<Integer> userIds = recruitmentProfiles.stream().map(r -> r.getUserId().intValue()).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(userIds)) {
                    List<UserRecruitmentPost> userRecruitmentPosts = userRecruitmentPostReps.findByUserIdInAndCompanyIdIn(userIds, companyIds);
                    if (!CollectionUtils.isEmpty(userRecruitmentPosts)) {
                        userRecruitmentPostReps.deleteAll(userRecruitmentPosts);
                    }
                }
            }
        }
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

    @Override
    public Map<Long, Integer> mapIndustryWithNumber() {
        List<UserRecruitmentPost> userPosts = userRecruitmentPostReps.findAll();

        Map<Long, List<Integer>> mapPostWithListUserId = new HashMap<>();
        if (!CollectionUtils.isEmpty(userPosts)) {
            List<Long> postIds = userPosts.stream().map(UserRecruitmentPost::getPostId).distinct().collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(postIds)) {
                for (Long p : postIds) {
                    for (UserRecruitmentPost u : userPosts) {
                        List<Integer> userIds = new ArrayList<>();
                        if (Objects.equals(p, u.getPostId())) {
                            userIds.add(u.getUserId());
                        }
                        mapPostWithListUserId.put(p, userIds);
                    }
                }
            }
        }

        // Lấy số lượng ứng tuyển theo bài viết
        List<Long> postIds = new ArrayList<>();
        Map<Long, Integer> mapPostWithNumberStd = new HashMap<>();
        if (!CollectionUtils.isEmpty(mapPostWithListUserId)) {
            for (Map.Entry<Long, List<Integer>> entry : mapPostWithListUserId.entrySet()) {
                Long k = entry.getKey();
                List<Integer> v = entry.getValue();
                mapPostWithNumberStd.put(k, v.size());
            }
        }

        if (!CollectionUtils.isEmpty(mapPostWithNumberStd)) {
            mapPostWithNumberStd = MapUtil.sortByValue(mapPostWithNumberStd);
            mapPostWithNumberStd.forEach((k, v) -> postIds.add(k));
        }

        List<Post> posts = postReps.findByIdIn(postIds);
        Map<Long, List<Long>> mapIdIndustryWithListPostId = new HashMap<>();

        // Lấy danh dánh bài viết của ngành nghề
        List<Long> industryIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(posts)) {
            industryIds = posts.stream().map(Post::getIndustryId).distinct().collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(industryIds)) {
                for (Long i : industryIds) {
                    List<Long> postIdList = new ArrayList<>();
                    for (Post p : posts) {
                        if (Objects.equals(i, p.getIndustryId())) {
                            postIdList.add(p.getId());
                        }
                    }
                    mapIdIndustryWithListPostId.put(i, postIdList);
                }
            }
        }

        // Lấy số lượng ứng tuyển theo tất cả bài viết thuộc ngành nghề đó
        Map<Long, Integer> mapIndustryWithNumber = new HashMap<>();
        if (!CollectionUtils.isEmpty(mapIdIndustryWithListPostId) && !CollectionUtils.isEmpty(mapPostWithNumberStd)) {
            for (Map.Entry<Long, List<Long>> entry : mapIdIndustryWithListPostId.entrySet()) {
                Long k = entry.getKey();
                List<Long> v = entry.getValue();

                Integer sumStd = 0;
                for (Map.Entry<Long, Integer> e : mapPostWithNumberStd.entrySet()) {
                    Long i = e.getKey();
                    Integer n = e.getValue();
                    if (!CollectionUtils.isEmpty(v) && v.contains(i)) {
                        sumStd += n;
                    }
                }

                mapIndustryWithNumber.put(k, sumStd);
            }
        }

        return mapIndustryWithNumber;
    }

    private Map<Long, CompanyDTO> setCompanyDTO(List<Long> companyIds) {
        Map<Long, CompanyDTO> companyDTOMap = new HashMap<>();
        if (companyIds != null && !companyIds.isEmpty()) {
            List<CompanyDTO> companyDTOS = companyReps.findByIdIn(companyIds).stream().map(companyMapper::to).collect(Collectors.toList());
            companyDTOMap = companyDTOS.stream().collect(Collectors.toMap(CompanyDTO::getId, c -> c));
        }
        return companyDTOMap;
    }

    private Map<Long, IndustryDTO> setIndustryDTO(List<Long> industryIds) {
        Map<Long, IndustryDTO> companyDTOMap = new HashMap<>();
        if (industryIds != null && !industryIds.isEmpty()) {
            List<IndustryDTO> industryDTOS = industryReps.findByIdIn(industryIds).stream().map(industryMapper::to).collect(Collectors.toList());
            companyDTOMap = industryDTOS.stream().collect(Collectors.toMap(IndustryDTO::getId, c -> c));
        }
        return companyDTOMap;
    }
}
