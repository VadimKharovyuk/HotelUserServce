package com.example.hoteluserservce.service;

import com.example.hoteluserservce.dto.PageResponse;
import com.example.hoteluserservce.dto.user.UserDto;
import org.springframework.data.domain.Pageable;


public interface AdminService {

    PageResponse<UserDto> getAllUsers(Pageable pageable);

}
