package com.hau.huylong.graduation_proejct.entity.hau;

import com.hau.huylong.graduation_proejct.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "user_recruitment_posts")
public class UserRecruitmentPost extends BaseEntity {
    private Integer userId;
    private Long postId;
    private Long companyId;
}
