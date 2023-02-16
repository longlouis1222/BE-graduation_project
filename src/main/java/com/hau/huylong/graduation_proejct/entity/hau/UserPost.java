package com.hau.huylong.graduation_proejct.entity.hau;

import com.hau.huylong.graduation_proejct.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "user_posts")
public class UserPost extends BaseEntity {
    private Integer userId;
    private Long postId;
}
