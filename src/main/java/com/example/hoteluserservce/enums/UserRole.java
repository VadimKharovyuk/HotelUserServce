package com.example.hoteluserservce.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    GUEST("Обычный гость"),
    HOTEL_OWNER("Владелец отеля"),
    ADMIN("Администратор системы");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}