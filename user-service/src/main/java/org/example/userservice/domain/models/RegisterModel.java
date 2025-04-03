package org.example.userservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterModel {
    private String name;
    private String email;
    private String password;
}
