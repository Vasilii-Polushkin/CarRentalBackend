package org.example.userservice.infrastructure.services;

/*
@Service
public class OidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserRepository userRepository;

    public OidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser oidcUser = new DefaultOidcUser(
                List.of(new OidcUserAuthority(request.getIdToken())),
                request.getIdToken()
        );

        saveOrUpdateUser(oidcUser);

        return oidcUser;
    }

    private void saveOrUpdateUser(OidcUser oidcUser) {
        userRepository.findById(UUID.fromString(oidcUser.getSubject()))
                .ifPresentOrElse(
                        user -> updateExistingUser(user, oidcUser),
                        () -> createNewUser(oidcUser)
                );
    }

    private void createNewUser(OidcUser oidcUser) {
        User user = new User();
        user.setId(UUID.fromString(oidcUser.getSubject()));
        user.setEmail(oidcUser.getEmail());
        user.setName(oidcUser.getFullName());
        user.setRoles(Set.of(Role.USER));

        userRepository.save(user);
    }

    private void updateExistingUser(User user, OidcUser oidcUser) {
        user.setEmail(oidcUser.getEmail());
        userRepository.save(user);
    }
}*/