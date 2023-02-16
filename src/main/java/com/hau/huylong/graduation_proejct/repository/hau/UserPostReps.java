package com.hau.huylong.graduation_proejct.repository.hau;

import com.hau.huylong.graduation_proejct.entity.hau.Post;
import com.hau.huylong.graduation_proejct.entity.hau.UserPost;
import com.hau.huylong.graduation_proejct.model.request.SearchPostRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPostReps extends JpaRepository<UserPost, Long> {
    List<UserPost> findByUserId(Integer userId);
    List<UserPost> findByPostIdAndUserId(Long postId, Integer userId);

    @Query("select i from Post i " +
            " WHERE (:#{#request.industryId} IS NULL OR i.industryId = :#{#request.industryId}) " +
            " AND  (i.id IN :postIds) " +
            " AND (:#{#request.salaryMax} IS NULL OR i.salaryMax <= :#{#request.salaryMax}) " +
            " AND (:#{#request.salaryMin} IS NULL OR i.salaryMin >= :#{#request.salaryMin}) " +
            " AND (:#{#request.companyId} IS NULL OR i.companyId = :#{#request.companyId}) " +
            " AND (:#{#request.jobApplicationDeadline} IS NULL OR i.jobApplicationDeadline = :#{#request.jobApplicationDeadline}) " +
            " AND (:#{#request.dateSubmit} IS NULL OR i.dateSubmit = :#{#request.dateSubmit}) " +
            " AND (:#{#request.title} IS NULL OR i.title LIKE %:#{#request.title}%) " +
            " AND (:#{#request.probationaryPeriod} IS NULL OR i.probationaryPeriod LIKE %:#{#request.probationaryPeriod}%) " +
            " AND (:#{#request.workplace} IS NULL OR i.workplace LIKE %:#{#request.workplace}%) " +
            " AND (:#{#request.necessarySkills} IS NULL OR i.necessarySkills LIKE %:#{#request.necessarySkills}%) " +
            " AND (:#{#request.level} IS NULL OR i.level LIKE %:#{#request.level}%) " +
            " AND (:#{#request.numberOfRecruits} IS NULL OR i.numberOfRecruits = :#{#request.numberOfRecruits}) " +
            " AND (:#{#request.recruitmentArea} IS NULL OR i.recruitmentArea LIKE %:#{#request.recruitmentArea}%) " +
            " AND (:#{#request.recruitmentGender} IS NULL OR i.recruitmentGender LIKE %:#{#request.recruitmentGender}%) " +
            " AND (:#{#request.recruitmentDegree} IS NULL OR i.recruitmentDegree LIKE %:#{#request.recruitmentDegree}%) " +
            " AND (:#{#request.recruitmentExperience} IS NULL OR i.recruitmentExperience = :#{#request.recruitmentExperience}) " +
            " AND (:#{#request.workingForm} IS NULL OR i.workingForm LIKE %:#{#request.workingForm}%) " +
            " AND (:#{#request.jobDescription} IS NULL OR i.jobDescription LIKE %:#{#request.jobDescription}%) " +
            " AND (:#{#request.jobRequirements} IS NULL OR i.jobRequirements LIKE %:#{#request.jobRequirements}%) " +
            " AND (:#{#request.benefits} IS NULL OR i.benefits LIKE %:#{#request.benefits}%) " +
            " AND (:#{#request.status} IS NULL OR i.status LIKE %:#{#request.status}%) " +
            " AND (:#{#request.isOutstanding} IS NULL OR i.isOutstanding = :#{#request.isOutstanding}) " +
            " ORDER BY i.id desc ")
    Page<Post> search(SearchPostRequest request, List<Long> postIds, Pageable pageable);
}
