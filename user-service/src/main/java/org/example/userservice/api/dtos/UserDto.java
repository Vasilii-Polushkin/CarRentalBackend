package org.example.userservice.api.dtos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.userservice.domain.enums.Role;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @NotNull
    @NotBlank
    private UUID id;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    private String email;

    private boolean isActive;

    private Set<Role> roles;
}
