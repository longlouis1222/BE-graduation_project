package com.hau.huylong.graduation_proejct.repository.hau;

import com.hau.huylong.graduation_proejct.entity.hau.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyReps extends JpaRepository<Company, Long> {
    Optional<Company> findByUserId(Integer userId);

    List<Company> findByUserIdIn(List<Integer> userIds);

    List<Company> findByIdIn(List<Long> companyIds);
}
