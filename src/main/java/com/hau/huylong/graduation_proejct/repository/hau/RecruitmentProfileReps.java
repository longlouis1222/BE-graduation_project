package com.hau.huylong.graduation_proejct.repository.hau;

import com.hau.huylong.graduation_proejct.entity.hau.RecruitmentProfile;
import com.hau.huylong.graduation_proejct.model.request.SearchRecruitmentProfileRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentProfileReps extends JpaRepository<RecruitmentProfile, Long> {
    @Query("select i from RecruitmentProfile i " +
            " WHERE (:#{#request.userId} IS NULL OR i.userId = :#{#request.userId}) " +
            " AND (:#{#request.positionOffer} IS NULL OR i.positionOffer LIKE %:#{#request.positionOffer}%) " +
            " AND i.permissionSearch IS TRUE " +
            " AND (:#{#request.levelDesire} IS NULL OR i.levelDesire LIKE %:#{#request.levelDesire}%) " +
            " AND (:#{#request.academyLevel} IS NULL OR i.academyLevel LIKE %:#{#request.academyLevel}%) " +
            " AND (:#{#request.career} IS NULL OR i.career LIKE %:#{#request.career}%) " +
            " AND (:#{#request.workForm} IS NULL OR i.workForm LIKE %:#{#request.workForm}%) " +
            " AND (:#{#request.currentLevel} IS NULL OR i.currentLevel LIKE %:#{#request.currentLevel}%) " +
            " AND (:#{#request.workAddress} IS NULL OR i.workAddress LIKE %:#{#request.workAddress}%) " +
            " AND (:#{#request.address} IS NULL OR i.address LIKE %:#{#request.address}%) " +
            " AND (:#{#request.careerTarget} IS NULL OR i.careerTarget LIKE %:#{#request.careerTarget}%) " +
            " AND (:#{#request.sortSkill} IS NULL OR i.sortSkill LIKE %:#{#request.sortSkill}%) " +
            " AND (:#{#request.offerSalary} IS NULL OR i.offerSalary = :#{#request.offerSalary}) " +
            " AND (:#{#request.fileId} IS NULL OR i.fileId = :#{#request.fileId}) " +
            " AND (:#{#request.experienceNumber} IS NULL OR i.experienceNumber = :#{#request.experienceNumber}) " +
            " AND (:#{#request.timeSubmit} IS NULL OR i.timeSubmit = :#{#request.timeSubmit}) " +
            " ORDER BY i.id desc ")
    Page<RecruitmentProfile> search(SearchRecruitmentProfileRequest request, Pageable pageable);

    @Query("select i from RecruitmentProfile i where (:userId IS NULL OR i.userId = :userId) " +
            "AND i.created = (select max(r.created) from RecruitmentProfile r where (:userId IS NULL OR r.userId = :userId)) ")
    Optional<RecruitmentProfile> findByUserId(Long userId);

    List<RecruitmentProfile> findByIdIn(List<Long> ids);
    List<RecruitmentProfile> findByUserIdIn(List<Long> userIds);

    @Query("select i from RecruitmentProfile i " +
            " WHERE (:#{#request.userId} IS NULL OR i.userId = :#{#request.userId}) " +
            " AND (:#{#request.positionOffer} IS NULL OR i.positionOffer LIKE %:#{#request.positionOffer}%) " +
//            " AND i.permissionSearch IS TRUE " +
            " AND i.id IN :profileIds " +
            " AND (:#{#request.levelDesire} IS NULL OR i.levelDesire LIKE %:#{#request.levelDesire}%) " +
            " AND (:#{#request.academyLevel} IS NULL OR i.academyLevel LIKE %:#{#request.academyLevel}%) " +
            " AND (:#{#request.career} IS NULL OR i.career LIKE %:#{#request.career}%) " +
            " AND (:#{#request.workForm} IS NULL OR i.workForm LIKE %:#{#request.workForm}%) " +
            " AND (:#{#request.currentLevel} IS NULL OR i.currentLevel LIKE %:#{#request.currentLevel}%) " +
            " AND (:#{#request.workAddress} IS NULL OR i.workAddress LIKE %:#{#request.workAddress}%) " +
            " AND (:#{#request.address} IS NULL OR i.address LIKE %:#{#request.address}%) " +
            " AND (:#{#request.careerTarget} IS NULL OR i.careerTarget LIKE %:#{#request.careerTarget}%) " +
            " AND (:#{#request.sortSkill} IS NULL OR i.sortSkill LIKE %:#{#request.sortSkill}%) " +
            " AND (:#{#request.offerSalary} IS NULL OR i.offerSalary = :#{#request.offerSalary}) " +
            " AND (:#{#request.fileId} IS NULL OR i.fileId = :#{#request.fileId}) " +
            " AND (:#{#request.experienceNumber} IS NULL OR i.experienceNumber = :#{#request.experienceNumber}) " +
            " AND (:#{#request.timeSubmit} IS NULL OR i.timeSubmit = :#{#request.timeSubmit}) " +
            " ORDER BY i.id desc ")
    Page<RecruitmentProfile> getListProfile(SearchRecruitmentProfileRequest request, List<Long> profileIds ,Pageable pageable);

    @Query("select i from RecruitmentProfile i " +
            " WHERE (:#{#request.userId} IS NULL OR i.userId = :#{#request.userId}) " +
            " AND (:#{#request.positionOffer} IS NULL OR i.positionOffer LIKE %:#{#request.positionOffer}%) " +
//            " AND i.permissionSearch IS TRUE " +
            " AND i.userId IN :userIds " +
            " AND (:#{#request.levelDesire} IS NULL OR i.levelDesire LIKE %:#{#request.levelDesire}%) " +
            " AND (:#{#request.academyLevel} IS NULL OR i.academyLevel LIKE %:#{#request.academyLevel}%) " +
            " AND (:#{#request.career} IS NULL OR i.career LIKE %:#{#request.career}%) " +
            " AND (:#{#request.workForm} IS NULL OR i.workForm LIKE %:#{#request.workForm}%) " +
            " AND (:#{#request.currentLevel} IS NULL OR i.currentLevel LIKE %:#{#request.currentLevel}%) " +
            " AND (:#{#request.workAddress} IS NULL OR i.workAddress LIKE %:#{#request.workAddress}%) " +
            " AND (:#{#request.address} IS NULL OR i.address LIKE %:#{#request.address}%) " +
            " AND (:#{#request.careerTarget} IS NULL OR i.careerTarget LIKE %:#{#request.careerTarget}%) " +
            " AND (:#{#request.sortSkill} IS NULL OR i.sortSkill LIKE %:#{#request.sortSkill}%) " +
            " AND (:#{#request.offerSalary} IS NULL OR i.offerSalary = :#{#request.offerSalary}) " +
            " AND (:#{#request.fileId} IS NULL OR i.fileId = :#{#request.fileId}) " +
            " AND (:#{#request.experienceNumber} IS NULL OR i.experienceNumber = :#{#request.experienceNumber}) " +
            " AND (:#{#request.timeSubmit} IS NULL OR i.timeSubmit = :#{#request.timeSubmit}) " +
            " ORDER BY i.id desc ")
    Page<RecruitmentProfile> getListProfileOfCandidate(SearchRecruitmentProfileRequest request, List<Long> userIds ,Pageable pageable);
}
