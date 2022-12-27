package com.hau.huylong.graduation_proejct.service;

import com.hau.huylong.graduation_proejct.entity.auth.User;
import com.hau.huylong.graduation_proejct.model.dto.auth.UserDTO;
import com.hau.huylong.graduation_proejct.model.request.UserRequest;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findByUsernameAndStatus(String username, short status);
    PageDataResponse<UserDTO> getAll(UserRequest request);
    UserDTO edit(Integer userId, UserRequest userRequest);
    void addRoleToUser(List<Integer> roleIds, List<Integer> userIds);
    void deleteUser(List<Integer> userIds);
    List<UserDTO> findById(List<Integer> userIds);
    void uploadAvatar(MultipartFile file, String filePath, boolean isPublic);
    void inActive(Integer userId, boolean check);
}
