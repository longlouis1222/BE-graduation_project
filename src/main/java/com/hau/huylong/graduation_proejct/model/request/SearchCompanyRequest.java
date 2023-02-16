package com.hau.huylong.graduation_proejct.model.request;

import lombok.Data;

@Data
public class SearchCompanyRequest extends SearchRequest{
    private String taxCode;
    private String name;
    private String emailCompany;
    private String employeeNumber;
    private String companyAddress;
    private String location;
    private String companyPhoneNumber;
    private String fieldOfActivity;
    private String businessLicense;
    private String businessIntroduction;
}
