package com.hau.huylong.graduation_proejct.controller.hau;

import com.hau.huylong.graduation_proejct.model.dto.auth.UserDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.IndustryDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.PostDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.RecruitmentProfileDTO;
import com.hau.huylong.graduation_proejct.model.request.SearchPostRequest;
import com.hau.huylong.graduation_proejct.model.request.SearchRecruitmentProfileRequest;
import com.hau.huylong.graduation_proejct.model.request.UserRequest;
import com.hau.huylong.graduation_proejct.model.response.APIResponse;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@Api(value = "API tài khoản")
public class UserController  {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<APIResponse<PageDataResponse<UserDTO>>> getAll(UserRequest request) {
        return ResponseEntity.ok(APIResponse.success(userService.getAll(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<UserDTO>> edit(@PathVariable Integer id, @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(APIResponse.success(userService.edit(id, userRequest)));
    }

    @DeleteMapping
    public ResponseEntity<APIResponse<Void>> delete(@RequestParam List<Integer> ids) {
        userService.deleteUser(ids);
        return ResponseEntity.ok(APIResponse.success());
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<APIResponse<UserDTO>> findById(@RequestParam List<Integer> ids) {
        return ResponseEntity.ok(APIResponse.success(userService.findById(ids).get(0)));
    }

    @GetMapping("/inactive")
    public ResponseEntity<APIResponse<Void>> inActiveAcc(@RequestParam Integer userId, @RequestParam boolean check) {
        userService.inActive(userId, check);
        return ResponseEntity.ok(APIResponse.success());
    }

    @PostMapping("/avatar")
    public ResponseEntity<APIResponse<Void>> uploadAvatar(@RequestParam("fileUpload") MultipartFile fileUpload,
                                                          @RequestParam("filePath") String pathFile,
                                                          @RequestParam("shared") String shared) {
        userService.uploadAvatar(fileUpload, pathFile, Boolean.parseBoolean(shared));
        return ResponseEntity.ok(APIResponse.success());
    }

    @ApiOperation(value = "API upload hồ sơ công ty")
    @PostMapping("/company-profile")
    public ResponseEntity<APIResponse<String>> uploadProfileCompany(@RequestParam("fileUpload") MultipartFile fileUpload,
                                                          @RequestParam("filePath") String pathFile,
                                                          @RequestParam("shared") String shared) {
        return ResponseEntity.ok(APIResponse.success(userService.uploadCompanyProfile(fileUpload, pathFile, Boolean.parseBoolean(shared))));
    }

    @GetMapping("/user-submit-topic")
    @ApiOperation(value = "Người dùng hiện tại ứng tuyển")
    public ResponseEntity<APIResponse<Void>> userCurrentSavePost(@RequestParam Long postId) {
        userService.userSubmitPostRecruitment(postId);
        return ResponseEntity.ok(APIResponse.success());
    }

    @GetMapping("/remove-submit-topic")
    @ApiOperation(value = "Xóa tin mà người dùng hiện tại ứng tuyển")
    public ResponseEntity<APIResponse<Void>> userCurrentRemovePost(@RequestParam Long postId) {
        userService.removePostByCurrentUserSubmit(postId);
        return ResponseEntity.ok(APIResponse.success());
    }

    @GetMapping("/get-list-topic-submit")
    @ApiOperation(value = "Lấy danh sách bài viết người dùng hiện tại đã ứng tuyển")
    public ResponseEntity<APIResponse<PageDataResponse<PostDTO>>> getListPostOfUserCurrent(SearchPostRequest request) {
        return ResponseEntity.ok(APIResponse.success(userService.getAllPostUserRecruitment(request)));
    }

    @GetMapping("/remove-profile-by-employee")
    @ApiOperation(value = "Nhà tuyển dụng xoa hồ sơ ứng tuyển")
    public ResponseEntity<APIResponse<Void>> removeProfileByEmployee(@RequestParam Long profileId) {
        userService.removeRecruitmentByEmployee(profileId);
        return ResponseEntity.ok(APIResponse.success());
    }

    @GetMapping("/get-list-topic-employee")
    @ApiOperation(value = "Lấy danh sách bài viết người dùng hiện tại đã ứng tuyển của nhà tyển dụng")
    public ResponseEntity<APIResponse<PageDataResponse<RecruitmentProfileDTO>>> getListPostOfEmployee(SearchRecruitmentProfileRequest request) {
        return ResponseEntity.ok(APIResponse.success(userService.getAllPostUserRecruitmentOfEmployee(request)));
    }

//    @GetMapping("/get-industry-hot")
//    @ApiOperation(value = "Lấy danh sách ngành nghề phổ biến")
//    public ResponseEntity<APIResponse<List<IndustryDTO>>> getListIndustryHot() {
//        return ResponseEntity.ok(APIResponse.success(userService.getListIndustryHot()));
//    }
}
