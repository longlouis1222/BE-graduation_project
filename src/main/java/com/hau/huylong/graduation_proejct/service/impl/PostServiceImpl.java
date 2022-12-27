package com.hau.huylong.graduation_proejct.service.impl;

import com.hau.huylong.graduation_proejct.common.enums.TypeUser;
import com.hau.huylong.graduation_proejct.common.exception.APIException;
import com.hau.huylong.graduation_proejct.common.util.BeanUtil;
import com.hau.huylong.graduation_proejct.common.util.PageableUtils;
import com.hau.huylong.graduation_proejct.entity.auth.CustomUser;
import com.hau.huylong.graduation_proejct.entity.auth.User;
import com.hau.huylong.graduation_proejct.entity.hau.Company;
import com.hau.huylong.graduation_proejct.entity.hau.Post;
import com.hau.huylong.graduation_proejct.model.dto.hau.CompanyDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.IndustryDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.PostDTO;
import com.hau.huylong.graduation_proejct.model.request.SearchPostRequest;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.repository.auth.UserReps;
import com.hau.huylong.graduation_proejct.repository.hau.CompanyReps;
import com.hau.huylong.graduation_proejct.repository.hau.IndustryReps;
import com.hau.huylong.graduation_proejct.repository.hau.PostReps;
import com.hau.huylong.graduation_proejct.service.PostService;
import com.hau.huylong.graduation_proejct.service.mapper.CompanyMapper;
import com.hau.huylong.graduation_proejct.service.mapper.IndustryMapper;
import com.hau.huylong.graduation_proejct.service.mapper.PostMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

        return postMapper.to(postReps.save(postMapper.from(postDTO)));
    }

    @Override
    public PostDTO edit(Long id, PostDTO postDTO) {
        Optional<Post> postOptional = postReps.findById(id);

        if (postOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy bài viết");
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

        Map<Long, IndustryDTO> industryDTOMap = new HashMap<>();
        if (postOptional.get().getIndustryId() != null) {
            industryDTOMap = setIndustryDTO(Collections.singletonList(postOptional.get().getIndustryId()));

            if (industryDTOMap.containsKey(postOptional.get().getIndustryId())) {
                postDTO.setIndustryDTO(industryDTOMap.get(postOptional.get().getIndustryId()));
            }
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
