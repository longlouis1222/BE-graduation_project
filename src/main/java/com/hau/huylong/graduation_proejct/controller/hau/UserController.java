package com.hau.huylong.graduation_proejct.controller.hau;

import com.hau.huylong.graduation_proejct.model.dto.auth.UserDTO;
import com.hau.huylong.graduation_proejct.model.request.UserRequest;
import com.hau.huylong.graduation_proejct.model.response.APIResponse;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.service.UserService;
import io.swagger.annotations.Api;
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

    @PostMapping
    public ResponseEntity<APIResponse<Void>> uploadAvatar(@RequestParam("fileUpload") MultipartFile fileUpload,
                                                          @RequestParam("filePath") String pathFile,
                                                          @RequestParam("shared") String shared) {
        userService.uploadAvatar(fileUpload, pathFile, Boolean.parseBoolean(shared));
        return ResponseEntity.ok(APIResponse.success());
    }
}
