package com.example.userservice.dto.entry;


import lombok.*;
@AllArgsConstructor
@Getter
@Setter
@Data
public class UserRegisterRequest {

    public Long getAuthId;

    public UserRegisterRequest(String email, String firstName, String lastName, String phone, String dni, Long authId) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.dni = dni;
        this.authId = authId;
    }

    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String dni;
    private Long authId;

}

