package com.example.hoteluserservce.service;

import com.example.hoteluserservce.dto.PageResponse;
import com.example.hoteluserservce.dto.user.RegisterRequest;
import com.example.hoteluserservce.dto.user.UserDto;
import org.springframework.data.domain.Pageable;


public interface UserService {

    UserDto registerUser(RegisterRequest request);




//    UserDto getUserById(Long id);
//    UserDto getUserByEmail(String email);
//    UserDto updateUser(Long id, UpdateUserRequest request);



}
