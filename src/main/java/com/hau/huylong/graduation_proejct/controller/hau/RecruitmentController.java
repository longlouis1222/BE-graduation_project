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
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/user-id")
    public ResponseEntity<APIResponse<RecruitmentProfileDTO>> findByUserId() {
        return ResponseEntity.ok(APIResponse.success(recruitmentProfileService.findByUserId()));
    }

    @GetMapping("/save-list")
    public ResponseEntity<APIResponse<Void>> saveList(@RequestParam List<Long> ids) {
        recruitmentProfileService.saveListProfile(ids);
        return ResponseEntity.ok(APIResponse.success());
    }

    @GetMapping("/get-list-id")
    public ResponseEntity<APIResponse<List<RecruitmentProfileDTO>>> getList(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(APIResponse.success(recruitmentProfileService.getByListProfileId(ids)));
    }

    @GetMapping("/get-view")
    public ResponseEntity<APIResponse<Integer>> getView(@RequestParam Long id) {
        return ResponseEntity.ok(APIResponse.success(recruitmentProfileService.viewProfile(id)));
    }

    @GetMapping("/active-search")
    public ResponseEntity<APIResponse<Void>> activeSearch(@RequestParam boolean check) {
        recruitmentProfileService.activeSearch(check);
        return ResponseEntity.ok(APIResponse.success());
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageDataResponse<RecruitmentProfileDTO>>> getAll(SearchRecruitmentProfileRequest request) {
        return ResponseEntity.ok(APIResponse.success(recruitmentProfileService.getAll(request)));
    }

    @PostMapping("/upload-profile")
    public ResponseEntity<APIResponse<Void>> uploadProfile(@RequestParam("fileUpload") MultipartFile fileUpload,
                                                          @RequestParam("filePath") String pathFile,
                                                          @RequestParam("shared") String shared) {
        recruitmentProfileService.uploadProfile(fileUpload, pathFile, Boolean.parseBoolean(shared));
        return ResponseEntity.ok(APIResponse.success());
    }
}
