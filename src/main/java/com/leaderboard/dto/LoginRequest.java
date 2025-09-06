package com.leaderboard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class LoginRequest {

    @NotBlank(message = "Username é obrigatorio")
    private String username;

    @NotBlank(message = "Password é obrigatorio")
    private String password;
}
