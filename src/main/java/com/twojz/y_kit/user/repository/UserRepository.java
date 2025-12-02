package com.twojz.y_kit.user.repository;

import com.twojz.y_kit.user.entity.LoginProvider;
import com.twojz.y_kit.user.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);
    Optional<UserEntity> findBySocialIdAndLoginProvider(String socialId, LoginProvider loginProvider);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByNickName(String nickName);
}