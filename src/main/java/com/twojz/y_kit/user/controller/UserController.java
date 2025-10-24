package com.twojz.y_kit.user.controller;

import com.twojz.y_kit.user.dto.request.LocalSignUpRequest;
import com.twojz.y_kit.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/v1/user/sign-up")
    public ResponseEntity<Void> signUp(@RequestBody LocalSignUpRequest request) {
        userService.localSignUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}