package com.leaderboard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Username é obrigatorio")
    @Size(min = 3, max = 50,message = "Username deve ter entre 3 e 50 caracteres" )
    private String username;

    @NotBlank(message = "Email é obrigatorio")
    @Email(message = "Email deve ser valido")
    @Size(max = 100,message = "Email deve ter no maximo 100 caracteres")
    private String email;

    @NotBlank(message = "Password é obrigatorio")
    @Size(min = 6, max = 100, message = "Password deve ter entre 6 e 100 caracteres")
    private String password;


}
