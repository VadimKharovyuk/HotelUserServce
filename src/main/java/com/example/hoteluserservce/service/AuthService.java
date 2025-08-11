package com.example.hoteluserservce.service;

import com.example.hoteluserservce.dto.AuthResponse;
import com.example.hoteluserservce.dto.LoginRequest;
import com.example.hoteluserservce.dto.RefreshTokenRequest;

import javax.security.auth.login.AccountLockedException;

public interface AuthService {

    AuthResponse authenticateUser(LoginRequest request) throws AccountLockedException;


//    AuthResponse refreshToken(RefreshTokenRequest request);
//    void logout(String refreshToken);
//    void logoutAll(Long userId);
}
