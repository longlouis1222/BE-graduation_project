package com.hau.huylong.graduation_proejct.repository.hau;

import com.hau.huylong.graduation_proejct.entity.auth.ResetPasswordToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetPasswordTokenReps extends CrudRepository<ResetPasswordToken, Long> {
    Optional<ResetPasswordToken> findByToken(String token);
}
