package com.hau.huylong.graduation_proejct.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface GenericService<DTO, SR> {
    DTO save(@RequestBody DTO dto);

    DTO edit(@PathVariable Long id, @RequestBody DTO dto);

    void delete(@PathVariable Long id);

    DTO findById(@PathVariable Long id);

    PageDataResponse<DTO> getAll(SR request);
}
