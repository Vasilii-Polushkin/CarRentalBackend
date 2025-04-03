package org.example.userservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequestModel {
    String password;
    String email;
}
