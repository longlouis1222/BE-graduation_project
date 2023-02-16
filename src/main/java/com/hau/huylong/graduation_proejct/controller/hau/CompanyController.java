package com.hau.huylong.graduation_proejct.controller.hau;

import com.hau.huylong.graduation_proejct.model.dto.hau.CompanyDTO;
import com.hau.huylong.graduation_proejct.model.dto.hau.PostDTO;
import com.hau.huylong.graduation_proejct.model.request.SearchCompanyRequest;
import com.hau.huylong.graduation_proejct.model.request.SearchPostRequest;
import com.hau.huylong.graduation_proejct.model.response.APIResponse;
import com.hau.huylong.graduation_proejct.model.response.PageDataResponse;
import com.hau.huylong.graduation_proejct.service.CompanyService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/companys")
@AllArgsConstructor
@Api(value = "API công ty")
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<CompanyDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(APIResponse.success(companyService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageDataResponse<CompanyDTO>>> getAll(SearchCompanyRequest request) {
        return ResponseEntity.ok(APIResponse.success(companyService.getAll(request)));
    }
}
