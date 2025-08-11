package com.example.hoteluserservce.service;

import com.example.hoteluserservce.dto.RegisterRequest;
import com.example.hoteluserservce.dto.UserDto;

import java.util.List;


public interface UserService {

    UserDto registerUser(RegisterRequest request);




//    UserDto getUserById(Long id);
//    UserDto getUserByEmail(String email);
//    UserDto updateUser(Long id, UpdateUserRequest request);
//    void deleteUser(Long id);
//    List<UserDto> getAllUsers();

}
