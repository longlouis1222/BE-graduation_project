package com.hau.huylong.graduation_proejct.entity.hau;

import com.hau.huylong.graduation_proejct.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "company")
public class Company extends BaseEntity {
    // mã số thuế
    @Column(name = "tax_code")
    private String taxCode;
    //Tên công ty
    @Column(name = "name")
    private String name;
    // Số lượng nhân viên
    @Column(name = "employee_number")
    private String employeeNumber;
    @Column(name = "address")
    private String companyAddress;
    @Column(name = "location")
    private String location;
    @Column(name = "email_company")
    private String emailCompany;
    @Column(name = "phone_number")
    private String companyPhoneNumber;
    @Column(name = "field_of_activity")
    private String fieldOfActivity;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "file_id")
    private String fileId;
    // giấy phép kinh doanh
    @Column(name = "business_license")
    private String businessLicense;
    // giới thiệu doanh nghiệp
    @Column(name = "business_introduction")
    private String businessIntroduction;
}
