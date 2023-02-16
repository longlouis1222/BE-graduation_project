package com.hau.huylong.graduation_proejct.repository.hau;

import com.hau.huylong.graduation_proejct.entity.hau.Company;
import com.hau.huylong.graduation_proejct.entity.hau.Industries;
import com.hau.huylong.graduation_proejct.model.request.SearchCompanyRequest;
import com.hau.huylong.graduation_proejct.model.request.SearchIndustryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyReps extends JpaRepository<Company, Long> {
    Optional<Company> findByUserId(Integer userId);

    List<Company> findByUserIdIn(List<Integer> userIds);

    List<Company> findByIdIn(List<Long> companyIds);

    @Query("select i from Company i " +
            " WHERE (:#{#request.taxCode} IS NULL OR i.taxCode = :#{#request.taxCode}) " +
            " AND (:#{#request.name} IS NULL OR i.name LIKE %:#{#request.name}%) " +
            " AND (:#{#request.emailCompany} IS NULL OR i.emailCompany LIKE %:#{#request.emailCompany}%) " +
            " AND (:#{#request.companyAddress} IS NULL OR i.companyAddress LIKE %:#{#request.companyAddress}%) " +
            " AND (:#{#request.location} IS NULL OR i.location LIKE %:#{#request.location}%) " +
            " AND (:#{#request.companyPhoneNumber} IS NULL OR i.companyPhoneNumber = :#{#request.companyPhoneNumber}) " +
            " AND (:#{#request.fieldOfActivity} IS NULL OR i.fieldOfActivity LIKE %:#{#request.fieldOfActivity}%) " +
            " AND (:#{#request.businessIntroduction} IS NULL OR i.businessIntroduction LIKE %:#{#request.businessIntroduction}%) " +
            " AND (:#{#request.businessLicense} IS NULL OR i.businessLicense LIKE %:#{#request.businessLicense}%) " +
            " AND (:#{#request.employeeNumber} IS NULL OR i.employeeNumber = :#{#request.emailCompany}) " +
            " ORDER BY i.id desc ")
    Page<Company> search(SearchCompanyRequest request, Pageable pageable);
}
