package com.example.hoteluserservce.service;

import com.example.hoteluserservce.dto.user.AuthResponse;
import com.example.hoteluserservce.dto.user.LoginRequest;
import com.example.hoteluserservce.dto.user.RefreshTokenRequest;

import javax.security.auth.login.AccountLockedException;

public interface AuthService {

    AuthResponse authenticateUser(LoginRequest request) throws AccountLockedException;


    AuthResponse refreshToken(RefreshTokenRequest request) throws AccountLockedException;


   void logout(String refreshToken);

}
