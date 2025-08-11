package com.example.hoteluserservce.service.impl;

import com.example.hoteluserservce.dto.PageResponse;
import com.example.hoteluserservce.dto.user.UserDto;
import com.example.hoteluserservce.mapper.UserMapper;
import com.example.hoteluserservce.model.User;
import com.example.hoteluserservce.repository.UserRepository;
import com.example.hoteluserservce.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public PageResponse<UserDto> getAllUsers(Pageable pageable) {
        log.info("Getting all users with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<User> userPage = userRepository.findAll(pageable);

        Page<UserDto> userDtoPage = userPage.map(userMapper::toUserDto);

        return PageResponse.from(userDtoPage);
    }
}
