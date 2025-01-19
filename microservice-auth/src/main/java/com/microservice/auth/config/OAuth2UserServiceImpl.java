package com.microservice.auth.config;
import com.microservice.auth.model.AuthUser;
import com.microservice.auth.model.OAuthProvider;
import com.microservice.auth.model.UserRole;
import com.microservice.auth.repositories.UserRepository;
import com.microservice.auth.repositories.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.client.oidc.userinfo.*;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = new OidcUserService().loadUser(userRequest);

        String email = oidcUser.getEmail();
        AuthUser user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new AuthUser();
            user.setFullName(oidcUser.getFullName() != null ? oidcUser.getFullName() : "NoName");
            user.setEmail(email);
            user.setPasswordHash("");
            user.setRole(UserRole.USER);
            user.setIsActive(true);
            user = userRepository.save(user);
            // Crear evento en Outbox
        }

        // Vincular el proveedor OAuth2
        String providerName = userRequest.getClientRegistration().getRegistrationId();
        String providerUserId = oidcUser.getName();
        Optional<OAuthProvider> existingProvider = providerRepository.findByProviderNameAndProviderUserId(providerName, providerUserId);
        if(!existingProvider.isPresent()) {
            OAuthProvider p = new OAuthProvider();
            p.setUser(user);
            p.setProviderName(providerName);
            p.setProviderUserId(providerUserId);
            providerRepository.save(p);
        }

        return oidcUser;
    }
}