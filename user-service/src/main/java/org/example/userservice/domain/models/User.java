package org.example.userservice.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.userservice.domain.enums.AuthProvider;
import org.example.userservice.domain.enums.Role;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    //private String providerId;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private Set<Role> roles = new HashSet<>();

    public User(String name, String email, String password, AuthProvider provider) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.provider = provider;
    }
}