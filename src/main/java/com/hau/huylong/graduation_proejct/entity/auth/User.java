
package com.hau.huylong.graduation_proejct.entity.auth;

import com.hau.huylong.graduation_proejct.common.enums.TypeUser;
import com.hau.huylong.graduation_proejct.common.util.JsonUtil;
import lombok.Data;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    public static final class Status {
        public static final short ACTIVE = 1;
        public static final short WAITING = 0;
        public static final short LOCK = -1;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    private Short status;

    @Column(name = "email")
    private String email;

    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    private TypeUser type;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
    @JoinTable(name = "user_role", joinColumns = { @JoinColumn(name = "user_id") },
            inverseJoinColumns = { @JoinColumn(name = "role_id") })
    private Set<Role> roles = new HashSet<Role>(0);

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }

    public Map<Short, String> getMapStatus() {
        Map<Short, String> mapStatus = new HashMap<>();
        mapStatus.put((short) 1, "ACTIVE");
        mapStatus.put((short) 0, "WAITING");
        mapStatus.put((short) -1, "LOCK");
        return mapStatus;
    }

    public Map<Short, String> getMapGender() {
        Map<Short, String> mapStatus = new HashMap<>();
        mapStatus.put((short) 0, "MALE");
        mapStatus.put((short) 1, "FEMALE");
        mapStatus.put((short) 2, "OTHER");
        return mapStatus;
    }
}
