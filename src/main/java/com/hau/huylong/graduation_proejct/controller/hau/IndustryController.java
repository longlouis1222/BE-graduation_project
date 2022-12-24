package com.hau.huylong.graduation_proejct.controller.hau;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hau.huylong.graduation_proejct.controller.APIController;
import com.hau.huylong.graduation_proejct.model.dto.auth.UserDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.IndustryDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.RecruitmentProfileDTO;
import com.hau.huylong.graduation_proejct.model.request.SearchIndustryRequest;
import com.hau.huylong.graduation_proejct.model.request.SearchRecruitmentProfileRequest;
import com.hau.huylong.graduation_proejct.model.request.UserRequest;
import com.hau.huylong.graduation_proejct.model.response.APIResponse;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.service.IndustryService;
import com.hau.huylong.graduation_proejct.service.RecruitmentProfileService;
import com.hau.huylong.graduation_proejct.service.UserService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/industries")
@AllArgsConstructor
@Api(value = "API ngành nghề")
public class IndustryController {
    private final IndustryService industryService;

    @PostMapping
    public ResponseEntity<APIResponse<IndustryDTO>> save(@RequestBody IndustryDTO industryDTO) throws JsonProcessingException {
        return ResponseEntity.ok(APIResponse.success(industryService.save(industryDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<IndustryDTO>> edit(@PathVariable Long id, @RequestBody IndustryDTO industryDTO) {
        return ResponseEntity.ok(APIResponse.success(industryService.edit(id, industryDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> delete(@PathVariable Long id) {
        industryService.delete(id);
        return ResponseEntity.ok(APIResponse.success());
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<IndustryDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(APIResponse.success(industryService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageDataResponse<IndustryDTO>>> getAll(SearchIndustryRequest request) {
        return ResponseEntity.ok(APIResponse.success(industryService.getAll(request)));
    }
}
