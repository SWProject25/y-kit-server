package com.twojz.y_kit.user;

import org.springframework.beans.factory.annotation.Autowired; // Autowired 추가
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// Lombok @RequiredArgsConstructor 제거
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 생성자 주입을 직접 구현 (Lombok @RequiredArgsConstructor 대체)
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원가입 로직
     */
    @Transactional
    public User registerUser(UserRegisterRequestDto requestDto) {

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // Lombok Builder 대신, User.java에서 정의한 생성자를 직접 사용
        User newUser = new User(
                requestDto.getEmail(),
                encodedPassword,
                requestDto.getName(),
                User.Role.USER,
                User.LoginProvider.LOCAL
        );

        return userRepository.save(newUser);
    }

    // ... login 메서드는 변경 없음 (User 객체 생성 부분이 아니므로)
    @Transactional(readOnly = true)
    public User login(UserLoginRequestDto requestDto) {
        // ... (앞서 제시된 login 로직 그대로)
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        return user;
    }
}