package com.github.nhordiienko23.mycustomauthserver.config;

import com.github.nhordiienko23.mycustomauthserver.repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class LoginSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private final UserRepository userRepository;

    public LoginSuccessListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            userRepository.findByUsername(username).ifPresent(user -> {
                user.setLastLoginAt(new Date());
                userRepository.save(user);
            });
        }
    }
}
