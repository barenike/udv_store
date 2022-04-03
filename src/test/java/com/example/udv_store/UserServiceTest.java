package com.example.udv_store;

import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.repository.RoleRepository;
import com.example.udv_store.model.repository.UserRepository;
import com.example.udv_store.model.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    public void createTest() {
        UserEntity user = new UserEntity();
        user.setEmail("haan@udv.ru");
        user.setPassword("123456Aa");

        String message = null;
        try {
            userService.create(user);
            userService.create(user);
        } catch (AccessDeniedException e) {
            message = e.getMessage();
        }
        assert message != null;
    }
}
