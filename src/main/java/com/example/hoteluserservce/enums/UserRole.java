package com.example.hoteluserservce.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("Клиент отеля"),              // Зарегистрированные клиенты
    HOTEL_OWNER("Владелец отеля"),     // Добавляют/управляют отелями
    ADMIN("Администратор системы");    // Полный доступ к системе

    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}