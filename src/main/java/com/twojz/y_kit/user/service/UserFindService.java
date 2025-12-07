package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.entity.UserEntity;
import com.twojz.y_kit.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFindService {
    private final UserRepository userRepository;

    public UserEntity findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    public UserEntity findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "가입되지 않은 이메일입니다."));
    }

    public UserEntity findUserWithRegion(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    public List<UserEntity> findUsersForProfileReminder(LocalDateTime sevenDaysAgo) {
        return userRepository.findUsersForProfileReminder(sevenDaysAgo);
    }
}
