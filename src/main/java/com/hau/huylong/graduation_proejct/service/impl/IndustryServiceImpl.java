package com.hau.huylong.graduation_proejct.service.impl;

import com.hau.huylong.graduation_proejct.common.exception.APIException;
import com.hau.huylong.graduation_proejct.common.util.BeanUtil;
import com.hau.huylong.graduation_proejct.common.util.PageableUtils;
import com.hau.huylong.graduation_proejct.entity.hau.Industries;
import com.hau.huylong.graduation_proejct.model.dto.hau.IndustryDTO;
import com.hau.huylong.graduation_proejct.model.request.SearchIndustryRequest;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.repository.hau.IndustryReps;
import com.hau.huylong.graduation_proejct.service.IndustryService;
import com.hau.huylong.graduation_proejct.service.UserService;
import com.hau.huylong.graduation_proejct.service.mapper.IndustryMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class IndustryServiceImpl implements IndustryService {
    private final IndustryReps industryReps;
    private final IndustryMapper industryMapper;
    private final UserService userService;

    @Override
    public IndustryDTO save(IndustryDTO industryDTO) {
        return industryMapper.to(industryReps.save(industryMapper.from(industryDTO)));
    }

    @Override
    public IndustryDTO edit(Long id, IndustryDTO industryDTO) {
        Optional<Industries> industriesOptional = industryReps.findById(id);

        if (industriesOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy ngành nghề");
        }

        Industries industries = industriesOptional.get();
        BeanUtil.copyNonNullProperties(industryDTO, industries);

        return industryMapper.to(industryReps.save(industries));
    }

    @Override
    public void delete(Long id) {
        Optional<Industries> industriesOptional = industryReps.findById(id);

        if (industriesOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy ngành nghề");
        }

        industryReps.delete(industriesOptional.get());
    }

    @Override
    public IndustryDTO findById(Long id) {
        Optional<Industries> industriesOptional = industryReps.findById(id);

        if (industriesOptional.isEmpty()) {
            throw APIException.from(HttpStatus.NOT_FOUND).withMessage("Không tìm thấy ngành nghề");
        }

        return industryMapper.to(industriesOptional.get());
    }

    @Override
    public PageDataResponse<IndustryDTO> getAll(SearchIndustryRequest request) {
        Pageable pageable = PageableUtils.of(request.getPage(), request.getSize());
        Page<IndustryDTO> page = industryReps.search(request, pageable).map(industryMapper::to);

        Map<Long, Integer> mapIndustryWithNumber = userService.mapIndustryWithNumber();
        if (!CollectionUtils.isEmpty(page.toList())) {
            page.forEach(p -> {
                if (!CollectionUtils.isEmpty(mapIndustryWithNumber) && mapIndustryWithNumber.containsKey(p.getId())) {
                    p.setNumberSummit(mapIndustryWithNumber.get(p.getId()));
                } else {
                    p.setNumberSummit(0);
                }
            });
        }
        return PageDataResponse.of(page);
    }
}
