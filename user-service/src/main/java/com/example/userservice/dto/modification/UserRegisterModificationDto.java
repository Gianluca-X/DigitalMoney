package com.example.userservice.dto.modification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterModificationDto {
    @NotNull(message = "Debe proveerse el id del usurio que se desea modificar")
    private Long id;
    @NotNull(message = "El username del usuario no puede ser nulo")
    @NotBlank(message = "Debe especificarse el username del usuario")
    private String userName;
    @NotNull(message = "El nombre del usuario no puede ser nulo")
    @NotBlank(message = "Debe especificarse el nombre del usuario")
    private String firstName;
    @NotNull(message = "El apellido del usuario no puede ser nulo")
    @NotBlank(message = "Debe especificarse el apellido del usuario")
    private String lastName;
    @NotNull(message = "El DNI del usuario no puede ser nulo")
    @NotBlank(message = "Debe especificarse el DNI del usuario")
    private int dni;
    @NotNull(message = "El telefono del usuario no puede ser nulo")
    @NotBlank(message = "Debe especificarse el telefono del usuario")
    private int phone;
    @NotNull(message = "El email del usuario no puede ser nulo")
    @NotBlank(message = "Debe especificarse el email del usuario")
    private String email;
    @NotNull(message = "La contraseña del usuario no puede ser nulo")
    @NotBlank(message = "Debe especificarse la contraseña del usuario")
    private String password;

}
