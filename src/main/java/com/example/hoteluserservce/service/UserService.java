package com.example.hoteluserservce.service;

import com.example.hoteluserservce.dto.PageResponse;
import com.example.hoteluserservce.dto.user.RegisterRequest;
import com.example.hoteluserservce.dto.user.UpdateUserDto;
import com.example.hoteluserservce.dto.user.UserDto;
import org.springframework.data.domain.Pageable;


public interface UserService {

    UserDto registerUser(RegisterRequest request);

    UserDto getUserById(Long userId);


    UserDto getUserByUsername(String username);

    UserDto updateUserByUsername(String username, UpdateUserDto updateDto);





}
