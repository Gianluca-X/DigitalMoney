package com.example.userservice.dto.entry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntryDto {
        @NotNull(message = "El nombre del usuario no puede ser nulo")
        @NotBlank(message = "Debe especificarse el nombre del usuario")
        private String firstName;
        @NotNull(message = "El apellido del usuario no puede ser nulo")
        @NotBlank(message = "Debe especificarse el apellido del usuario")
        private String lastName;
        @NotNull(message = "El DNI del usuario no puede ser nulo")
        @NotBlank(message = "Debe especificarse el DNI del usuario")
        private String dni;
        @NotNull(message = "El telefono del usuario no puede ser nulo")
        @NotBlank(message = "Debe especificarse el telefono del usuario")
        private String phone;
        @NotNull(message = "El email del usuario no puede ser nulo")
        @NotBlank(message = "Debe especificarse el email del usuario")
        private String email;
        @NotNull(message = "La contraseña del usuario no puede ser nulo")
        @NotBlank(message = "Debe especificarse la contraseña del usuario")
        private String password;
        private Long authId;
        private Set<String> roles;

    }
