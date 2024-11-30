package com.example.cliniccare.config;

import com.example.cliniccare.entity.Role;
import com.example.cliniccare.entity.User;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.repository.RoleRepository;
import com.example.cliniccare.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws ServletException, IOException {

        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;

        if ("google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {
            DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = new HashMap<>(principal.getAttributes());

            String email = attributes.getOrDefault("email", "").toString();
            String name = attributes.getOrDefault("name", "").toString();

            userRepository.findByEmailAndDeleteAtIsNull(email)
                    .ifPresentOrElse(user -> {
                        Authentication securityAuth = getAuthentication(user, attributes, oAuth2AuthenticationToken);
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    }, () -> {
                        Role userRole = roleRepository.findByNameIgnoreCase("user").orElseThrow(
                                () -> new BadRequestException("Default role not found")
                        );

                        User user = new User();
                        user.setRole(userRole);
                        user.setEmail(email);
                        user.setName(name);
                        userRepository.save(user);

                        attributes.put("id", user.getUserId().toString());
                        Authentication securityAuth = getAuthentication(user, attributes, oAuth2AuthenticationToken);
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    });
        }

        this.setAlwaysUseDefaultTargetUrl(true);
        this.setDefaultTargetUrl(frontendUrl);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private static Authentication getAuthentication(
            User user,
            Map<String, Object> attributes,
            OAuth2AuthenticationToken oAuth2AuthenticationToken
    ) {
        attributes.putIfAbsent("id", user.getUserId().toString());

        DefaultOAuth2User newUser = new DefaultOAuth2User(List.of(
                new SimpleGrantedAuthority(user.getRole().getName())),
                attributes,
                "id"
        );

        return new OAuth2AuthenticationToken(
                newUser,
                List.of(new SimpleGrantedAuthority(user.getRole().getName())),
                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
        );
    }
}
