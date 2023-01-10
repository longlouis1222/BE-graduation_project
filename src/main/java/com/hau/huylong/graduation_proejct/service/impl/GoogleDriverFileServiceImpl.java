package com.hau.huylong.graduation_proejct.service.impl;

import com.google.api.services.drive.model.File;
import com.hau.huylong.graduation_proejct.config.ggdriver.GoogleFileManager;
import com.hau.huylong.graduation_proejct.convert.ConvertByteToMB;
import com.hau.huylong.graduation_proejct.model.dto.hau.GoogleDriverFileDTO;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.service.GoogleDriverFile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class GoogleDriverFileServiceImpl implements GoogleDriverFile {
    private final GoogleFileManager googleFileManager;

    @Override
    public PageDataResponse<GoogleDriverFileDTO> getAllFile() throws IOException, GeneralSecurityException {
        List<GoogleDriverFileDTO> responseList = getListFile();
        return PageDataResponse.of(String.valueOf(responseList.size()), responseList);
    }

    private List<GoogleDriverFileDTO> getListFile() throws GeneralSecurityException, IOException {
        List<GoogleDriverFileDTO> responseList = new ArrayList<>();
        List<File> files = googleFileManager.listEverything();
        GoogleDriverFileDTO dto = null;

        if (files != null) {
            responseList = new ArrayList<>();
            for (File f : files) {
                dto = new GoogleDriverFileDTO();
                if (f.getSize() != null) {
                    dto.setId(f.getId());
                    dto.setName(f.getName());
                    dto.setThumbnailLink(f.getThumbnailLink());
                    dto.setSize(ConvertByteToMB.getSize(f.getSize()));
                    dto.setLink("https://drive.google.com/file/d/" + f.getId() + "/view?usp=sharing");
                    dto.setShared(f.getShared());

                    responseList.add(dto);
                }
            }
        }

        return responseList;
    }

    @Override
    public void deleteFile(String id) throws Exception {
        googleFileManager.deleteFileOrFolder(id);
    }

    @Override
    public String uploadFile(MultipartFile file, String filePath, boolean isPublic) {
        String type = "";
        String role = "";
        if (isPublic) {
            // Public file of folder for everyone
            type = "anyone";
            role = "reader";
        } else {
            // Private
            type = "private";
            role = "private";
        }
        return googleFileManager.uploadFile(file, filePath, type, role);
    }

    @Override
    public byte[] downloadFile(String id, OutputStream outputStream, HttpServletResponse response) throws IOException, GeneralSecurityException {
        File file = googleFileManager.getFileById(id)
                ;
        response.setContentType(file.getMimeType());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
        googleFileManager.downloadFile(id, outputStream);
        outputStream = new ByteArrayOutputStream();
        return ((ByteArrayOutputStream) outputStream).toByteArray();
    }

    @Override
    public GoogleDriverFileDTO findByIdFiled(String fileId) throws GeneralSecurityException, IOException {
        List<GoogleDriverFileDTO> responseList = getListFile();

        GoogleDriverFileDTO googleDriverFileDTO = null;
        if (!responseList.isEmpty()) {
            for (GoogleDriverFileDTO r : responseList) {
                if (r.getId().equals(fileId)) {
                    googleDriverFileDTO = r;
                }
            }
        }

        return googleDriverFileDTO;
    }


}
