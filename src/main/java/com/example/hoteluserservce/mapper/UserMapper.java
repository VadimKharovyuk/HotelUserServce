package com.example.hoteluserservce.mapper;

import com.example.hoteluserservce.dto.user.UpdateUserDto;
import com.example.hoteluserservce.dto.user.UserDto;
import com.example.hoteluserservce.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setPhone(user.getPhone());
        userDto.setRole(user.getRole());
        userDto.setEmailVerified(user.isEmailVerified());
        userDto.setCreatedAt(user.getCreatedAt());


        return userDto;
    }

    public User toUser(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        return User.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .phone(userDto.getPhone())
                .role(userDto.getRole())
                .emailVerified(userDto.isEmailVerified())
                .build();
    }



    /**
     * Обновляет существующую сущность User данными из UpdateUserDto
     * Обновляет только не-null поля
     */
    public void updateUserFromDto(User existingUser, UpdateUserDto updateDto) {
        if (updateDto == null || existingUser == null) {
            return;
        }

        if (updateDto.getUsername() != null) {
            existingUser.setUsername(updateDto.getUsername());
        }

        if (updateDto.getFirstName() != null) {
            existingUser.setFirstName(updateDto.getFirstName());
        }

        if (updateDto.getLastName() != null) {
            existingUser.setLastName(updateDto.getLastName());
        }

        if (updateDto.getEmail() != null) {
            existingUser.setEmail(updateDto.getEmail());
        }

        if (updateDto.getPhone() != null) {
            existingUser.setPhone(updateDto.getPhone());
        }
    }

}
