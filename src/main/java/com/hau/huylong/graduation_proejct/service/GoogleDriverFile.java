package com.hau.huylong.graduation_proejct.service;

import com.hau.huylong.graduation_proejct.model.dto.hau.GoogleDriverFileDTO;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public interface GoogleDriverFile {
    PageDataResponse<GoogleDriverFileDTO> getAllFile() throws IOException, GeneralSecurityException;
    void deleteFile(String id) throws Exception;
    String uploadFile(MultipartFile file, String filePath, boolean isPublic);
    byte[] downloadFile(String id, OutputStream outputStream, HttpServletResponse response) throws IOException, GeneralSecurityException;
    GoogleDriverFileDTO findByIdFiled(String fileId) throws GeneralSecurityException, IOException;
}
