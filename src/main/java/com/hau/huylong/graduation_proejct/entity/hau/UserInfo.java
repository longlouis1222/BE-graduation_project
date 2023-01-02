package com.hau.huylong.graduation_proejct.entity.hau;

import com.hau.huylong.graduation_proejct.common.enums.TypeUser;
import com.hau.huylong.graduation_proejct.entity.BaseEntity;
import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "user_info")
public class UserInfo extends BaseEntity {
    public static final class Gender {
        public static final short MALE = 0;
        public static final short FEMALE = 1;
        public static final short OTHER = 2;
    }

    @Column(name = "fullname")
    private String fullName;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "date_of_birth")
    private Instant dateOfBirth;

    @Column(name = "town")
    private String town;

    @Column(name = "gender")
    private short gender;

    @Column(name = "marriage_status")
    private String marriageStatus;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    private TypeUser type;

    @Column(name = "companyId")
    private Long companyId;

    @Column(name = "arr_recruitment_id")
    private String arrRecruitmentIds;
}
