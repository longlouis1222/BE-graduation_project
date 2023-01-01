package com.hau.huylong.graduation_proejct.repository.hau;

import com.hau.huylong.graduation_proejct.entity.hau.Post;
import com.hau.huylong.graduation_proejct.model.request.SearchPostRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostReps extends JpaRepository<Post, Long> {
    @Query("select i from Post i " +
            " WHERE (:#{#request.industryId} IS NULL OR i.industryId = :#{#request.industryId}) " +
            " AND (:#{#request.salaryMax} IS NULL OR i.salaryMax = :#{#request.salaryMax}) " +
            " AND (:#{#request.salaryMin} IS NULL OR i.salaryMin = :#{#request.salaryMin}) " +
            " AND (:#{#request.recruitmentArea} IS NULL OR i.recruitmentArea LIKE %:#{#request.recruitmentArea}%) " +
            " AND (:#{#request.recruitmentDegree} IS NULL OR i.recruitmentDegree LIKE %:#{#request.recruitmentDegree}%) " +
            " AND (:#{#request.recruitmentExperience} IS NULL OR i.recruitmentExperience LIKE %:#{#request.recruitmentExperience}%) " +
            " AND (:#{#request.workingForm} IS NULL OR i.workingForm LIKE %:#{#request.workingForm}%) " +
            " AND (:#{#request.jobDescription} IS NULL OR i.jobDescription LIKE %:#{#request.jobDescription}%) " +
            " AND (:#{#request.jobRequirements} IS NULL OR i.jobRequirements LIKE %:#{#request.jobRequirements}%) " +
            " AND (:#{#request.benefits} IS NULL OR i.benefits LIKE %:#{#request.benefits}%) " +
            " AND (:#{#request.status} IS NULL OR i.status LIKE %:#{#request.status}%) " +
            " ORDER BY i.id desc ")
    Page<Post> search(SearchPostRequest request, Pageable pageable);

    List<Post> findByIdIn(List<Long> postIds);
}
