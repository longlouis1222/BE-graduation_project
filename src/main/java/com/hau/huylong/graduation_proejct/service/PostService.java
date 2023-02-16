package com.hau.huylong.graduation_proejct.service;

import com.hau.huylong.graduation_proejct.model.dto.hau.PostDTO;
import com.hau.huylong.graduation_proejct.model.request.SearchPostRequest;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;

public interface PostService extends GenericService<PostDTO, SearchPostRequest> {
    void currentUserSavePost(Long postId);
    void removePostByCurrentUserSave(Long postId);
    Integer viewProfile(Long postId);
    PageDataResponse<PostDTO> getAllPostCurrentUserSave(SearchPostRequest request);
}
