package com.example.hoteluserservce.controller;

import com.example.hoteluserservce.dto.PageResponse;
import com.example.hoteluserservce.dto.user.UserDto;
import com.example.hoteluserservce.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/api/admin")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public PageResponse<UserDto> getAllUsers(Pageable pageable) {
        return adminService.getAllUsers(pageable);
    }
}