package com.hau.huylong.graduation_proejct.controller.hau;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hau.huylong.graduation_proejct.controller.APIController;
import com.hau.huylong.graduation_proejct.model.dto.auth.UserDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.RecruitmentProfileDTO;
import com.hau.huylong.graduation_proejct.model.request.SearchRecruitmentProfileRequest;
import com.hau.huylong.graduation_proejct.model.request.UserRequest;
import com.hau.huylong.graduation_proejct.model.response.APIResponse;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.service.RecruitmentProfileService;
import com.hau.huylong.graduation_proejct.service.UserService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recruitments")
@AllArgsConstructor
@Api(value = "API hồ sơ tuyển dụng")
public class RecruitmentController {
    private final RecruitmentProfileService recruitmentProfileService;

    @PostMapping
    public ResponseEntity<APIResponse<RecruitmentProfileDTO>> save(@RequestBody RecruitmentProfileDTO recruitmentProfileDTO) throws JsonProcessingException {
        return ResponseEntity.ok(APIResponse.success(recruitmentProfileService.save(recruitmentProfileDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<RecruitmentProfileDTO>> edit(@PathVariable Long id, @RequestBody RecruitmentProfileDTO recruitmentProfileDTO) {
        return ResponseEntity.ok(APIResponse.success(recruitmentProfileService.edit(id, recruitmentProfileDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> delete(@PathVariable Long id) {
        recruitmentProfileService.delete(id);
        return ResponseEntity.ok(APIResponse.success());
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<RecruitmentProfileDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(APIResponse.success(recruitmentProfileService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageDataResponse<RecruitmentProfileDTO>>> getAll(SearchRecruitmentProfileRequest request) {
        return ResponseEntity.ok(APIResponse.success(recruitmentProfileService.getAll(request)));
    }
}
