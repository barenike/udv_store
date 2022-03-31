package com.example.udv_store.configuration;

import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.service.UserService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserEntity user = userService.findByUserId(userId);
        return CustomUserDetails.fromUserEntityToCustomUserDetails(user);
    }
}
