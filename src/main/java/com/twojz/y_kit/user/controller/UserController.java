package com.twojz.y_kit.user.controller;

import com.twojz.y_kit.user.dto.response.UserResponse;
import com.twojz.y_kit.user.entity.User;
import com.twojz.y_kit.user.service.UserService;
import com.twojz.y_kit.user.dto.request.UserLoginRequest;
import com.twojz.y_kit.user.dto.request.UserRegisterRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterRequest requestDto) {
        userService.registerUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest requestDto) {
        User user = userService.login(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body("로그인 성공! 사용자 ID: " + UserResponse.from(user).getId());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        if (e.getMessage().contains("로그인 실패") || e.getMessage().contains("일치하지 않습니다")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다." + e.getMessage());
    }
}