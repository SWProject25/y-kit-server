package com.twojz.y_kit.user.service;

import com.twojz.y_kit.user.entity.LoginProvider;
import com.twojz.y_kit.user.entity.Role;
import com.twojz.y_kit.user.entity.User;
import com.twojz.y_kit.user.dto.request.UserLoginRequest;
import com.twojz.y_kit.user.dto.request.UserRegisterRequest;
import com.twojz.y_kit.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired; // Autowired 추가
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입 로직
    @Transactional
    public User registerUser(UserRegisterRequest requestDto) {

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User newUser = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .name(requestDto.getName())
                .role(Role.USER)
                .loginProvider(LoginProvider.LOCAL)
                .build();

        return userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public User login(UserLoginRequest requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        return user;
    }
}