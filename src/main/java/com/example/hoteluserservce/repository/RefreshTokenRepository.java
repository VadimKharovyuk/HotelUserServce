package com.example.hoteluserservce.repository;

import com.example.hoteluserservce.model.RefreshToken;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken>findByToken(String refreshToken);
}
