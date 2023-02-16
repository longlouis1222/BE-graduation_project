package com.hau.huylong.graduation_proejct.service.impl;

import com.hau.huylong.graduation_proejct.common.enums.TypeUser;
import com.hau.huylong.graduation_proejct.common.exception.APIException;
import com.hau.huylong.graduation_proejct.common.util.BeanUtil;
import com.hau.huylong.graduation_proejct.common.util.PageableUtils;
import com.hau.huylong.graduation_proejct.entity.auth.CustomUser;
import com.hau.huylong.graduation_proejct.entity.auth.User;
import com.hau.huylong.graduation_proejct.entity.hau.*;
import com.hau.huylong.graduation_proejct.model.dto.auth.UserDTO;
import com.hau.huylong.graduation_proejct.model.dto.auth.UserInfoDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.CompanyDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.IndustryDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.PostDTO;
import com.hau.huylong.graduation_proejct.model.request.SearchPostRequest;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.repository.auth.UserInfoReps;
import com.hau.huylong.graduation_proejct.repository.auth.UserReps;
import com.hau.huylong.graduation_proejct.repository.hau.*;
import com.hau.huylong.graduation_proejct.service.PostService;
import com.hau.huylong.graduation_proejct.service.mapper.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.hau.huylong.graduation_proejct.common.constant.Constants.Post.Status.*;

@Service
@AllArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {
    private final UserReps userReps;
    private final PostReps postReps;
    private final PostMapper postMapper;
    private final CompanyReps companyReps;
    private final CompanyMapper companyMapper;
    private final IndustryReps industryReps;
    private final IndustryMapper industryMapper;
    private final UserPostReps userPostReps;
    private final UserInfoReps userInfoReps;
    private final UserInfoMapper userInfoMapper;
    private final UserMapper userMapper;
    private final UserRecruitmentPostReps userRecruitmentPostReps;

    @Override
    public PostDTO save(PostDTO postDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();

        Optional<User> userOptional = userReps.findById(user.getId());

        if (userOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không thể tìm thấy người dùng với id  " + userOptional);
        }

        if (userOptional.get().getType().name().equalsIgnoreCase(TypeUser.EMPLOYER.name())) {
            Company company = companyReps.findByUserId(user.getId())
                    .orElseThrow(() -> APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy công ty"));
            postDTO.setCompanyId(company.getId());
        }

        postDTO.setStatus(WAITING_APPROVE.name());
        postDTO.setIsOutstanding(false);

        return postMapper.to(postReps.save(postMapper.from(postDTO)));
    }

    @Override
    public PostDTO edit(Long id, PostDTO postDTO) {
        Optional<Post> postOptional = postReps.findById(id);

        if (postOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy bài viết");
        }

        if (postDTO.getStatus().equalsIgnoreCase(WAITING_APPROVE.name())) {
            postDTO.setStatus(WAITING_APPROVE.name());
        } else if (postDTO.getStatus().equalsIgnoreCase(APPROVED.name())) {
            postDTO.setStatus(APPROVED.name());
        } else {
            postDTO.setStatus(REJECT.name());
        }

        Post post = postOptional.get();
        BeanUtil.copyNonNullProperties(postDTO, post);

        return postMapper.to(postReps.save(post));
    }

    @Override
    public void delete(Long id) {
        Optional<Post> postOptional = postReps.findById(id);

        if (postOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy bài viết");
        }

        postReps.delete(postOptional.get());
    }

    @Override
    public PostDTO findById(Long id) {
        Optional<Post> postOptional = postReps.findById(id);

        if (postOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy bài viết");
        }

        PostDTO postDTO = postMapper.to(postOptional.get());
        Map<Long, CompanyDTO> companyDTOMap = new HashMap<>();
        if (postOptional.get().getCompanyId() != null) {
            companyDTOMap = setCompanyDTO(Collections.singletonList(postOptional.get().getCompanyId()));

            if (companyDTOMap.containsKey(postOptional.get().getCompanyId())) {
                postDTO.setCompanyDTO(companyDTOMap.get(postOptional.get().getCompanyId()));
            }
        }

        Map<Long, Boolean> mapPostStatusSave = mapPostStatusSave();
        Map<Long, Boolean> mapPostStatusSubmit = mapPostStatusSubmit();

        Map<Long, IndustryDTO> industryDTOMap = new HashMap<>();
        if (postOptional.get().getIndustryId() != null) {
            industryDTOMap = setIndustryDTO(Collections.singletonList(postOptional.get().getIndustryId()));

            if (industryDTOMap.containsKey(postOptional.get().getIndustryId())) {
                postDTO.setIndustryDTO(industryDTOMap.get(postOptional.get().getIndustryId()));
            }
        }

        if (!CollectionUtils.isEmpty(mapPostStatusSave) && mapPostStatusSave.containsKey(postDTO.getId())) {
            postDTO.setUserCurrentSaved(mapPostStatusSave.get(postDTO.getId()));
        }

        if (!CollectionUtils.isEmpty(mapPostStatusSubmit) && mapPostStatusSubmit.containsKey(postDTO.getId())) {
            postDTO.setUserCurrentSubmited(mapPostStatusSubmit.get(postDTO.getId()));
        }

        return postDTO;
    }

    @Override
    public PageDataResponse<PostDTO> getAll(SearchPostRequest request) {
        Pageable pageable = PageableUtils.of(request.getPage(), request.getSize());
        Page<PostDTO> page = postReps.search(request, pageable).map(postMapper::to);

        if (!page.isEmpty()) {
            List<Long> companyIds = page.stream().map(PostDTO::getCompanyId).collect(Collectors.toList());
            List<Long> industryIds = page.stream().map(PostDTO::getIndustryId).collect(Collectors.toList());
            Map<Long, CompanyDTO> companyDTOMap = setCompanyDTO(companyIds);
            Map<Long, IndustryDTO> industryDTOMap = setIndustryDTO(industryIds);
            Map<Long, Boolean> mapPostStatusSave = mapPostStatusSave();
            Map<Long, Boolean> mapPostStatusSubmit = mapPostStatusSubmit();

            page.forEach(p -> {
                if (companyDTOMap.containsKey(p.getCompanyId())) {
                    p.setCompanyDTO(companyDTOMap.get(p.getCompanyId()));
                }

                if (industryDTOMap.containsKey(p.getIndustryId())) {
                    p.setIndustryDTO(industryDTOMap.get(p.getIndustryId()));
                }

                if (!CollectionUtils.isEmpty(mapPostStatusSave) && mapPostStatusSave.containsKey(p.getId())) {
                    p.setUserCurrentSaved(mapPostStatusSave.get(p.getId()));
                }

                if (!CollectionUtils.isEmpty(mapPostStatusSubmit) && mapPostStatusSubmit.containsKey(p.getId())) {
                    p.setUserCurrentSubmited(mapPostStatusSubmit.get(p.getId()));
                }
            });
        }

        return PageDataResponse.of(page);
    }

    private Map<Long, CompanyDTO> setCompanyDTO(List<Long> companyIds) {
        Map<Long, CompanyDTO> companyDTOMap = new HashMap<>();
        if (companyIds != null && !companyIds.isEmpty()) {
            List<CompanyDTO> companyDTOS = companyReps.findByIdIn(companyIds).stream().map(companyMapper::to).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(companyDTOS)) {
                List<Integer> userIds = companyDTOS.stream().map(CompanyDTO::getUserId).collect(Collectors.toList());
                Map<Integer, UserDTO> mapUserDTO = userReps.findByIds(userIds)
                        .stream().map(userMapper::to).collect(Collectors.toMap(UserDTO::getId, u -> u));
                Map<Integer, UserInfoDTO> mapUseInfoDTO = userInfoReps.findByUserIdIn(userIds)
                        .stream().map(userInfoMapper::to).collect(Collectors.toMap(UserInfoDTO::getUserId, u -> u));

                companyDTOS.forEach(c -> {
                    if (!CollectionUtils.isEmpty(mapUserDTO) && mapUserDTO.containsKey(c.getUserId())) {
                        c.setUserDTO(mapUserDTO.get(c.getUserId()));
                    }

                    if (!CollectionUtils.isEmpty(mapUseInfoDTO) && mapUseInfoDTO.containsKey(c.getUserId())) {
                        c.setUserInfoDTO(mapUseInfoDTO.get(c.getUserId()));
                    }
                });
            }

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

    @Override
    public void currentUserSavePost(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();

        Optional<Post> postOptional = postReps.findById(postId);
        if (postOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy bài viết");
        }

        UserPost userPost = new UserPost();
        userPost.setPostId(postOptional.get().getId());
        userPost.setUserId(user.getId());
        userPostReps.save(userPost);
    }

    @Override
    public void removePostByCurrentUserSave(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();
        List<UserPost> userPosts = userPostReps.findByPostIdAndUserId(postId, user.getId());

        if (!CollectionUtils.isEmpty(userPosts)) {
            userPostReps.deleteAll(userPosts);
        }
    }

    private Map<Long, Boolean> mapPostStatusSave() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();

        List<UserPost> userPosts = userPostReps.findByUserId(user.getId());

        Map<Long, Boolean> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(userPosts)) {
            userPosts.forEach(u -> {
                if (Objects.equals(u.getUserId(), user.getId())) {
                    map.put(u.getPostId(), true);
                }
            });
        }

        return map;
    }

    private Map<Long, Boolean> mapPostStatusSubmit() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();

        List<UserRecruitmentPost> userPosts = userRecruitmentPostReps.findByUserId(user.getId());

        Map<Long, Boolean> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(userPosts)) {
            userPosts.forEach(u -> {
                if (Objects.equals(u.getUserId(), user.getId())) {
                    map.put(u.getPostId(), true);
                }
            });
        }

        return map;
    }

    @Override
    public Integer viewProfile(Long postId) {
        Optional<Post> postOptional = postReps.findById(postId);

        if (postOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy hồ sơ tuyển dụng");
        }

        if (postOptional.get().getView() == null) {
            postOptional.get().setView(0);
        }

        postOptional.get().setView(postOptional.get().getView() + 1);
        postReps.save(postOptional.get());

        return postOptional.get().getView();
    }

    @Override
    public PageDataResponse<PostDTO> getAllPostCurrentUserSave(SearchPostRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();

        List<UserPost> userPosts = userPostReps.findByUserId(user.getId());
        List<Long> postIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userPosts)) {
            postIds = userPosts.stream().map(UserPost::getPostId).distinct().collect(Collectors.toList());
        }

        Pageable pageable = PageableUtils.of(request.getPage(), request.getSize());
        Page<PostDTO> page = userPostReps.search(request, postIds, pageable).map(postMapper::to);

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
}
