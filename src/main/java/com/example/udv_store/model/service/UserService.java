package com.example.udv_store.model.service;

import com.example.udv_store.infrastructure.user.RegistrationRequest;
import com.example.udv_store.model.entity.RoleEntity;
import com.example.udv_store.model.entity.UserEntity;
import com.example.udv_store.model.repository.RoleRepository;
import com.example.udv_store.model.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void create(RegistrationRequest registrationRequest) {
        if (findByEmail(registrationRequest.getEmail()) != null) {
            throw new AccessDeniedException("This email is already registered.");
        }
        UserEntity user = new UserEntity();
        RoleEntity userRole = roleRepository.findByName("ROLE_USER");
        user.setRoleEntity(userRole);
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setUserBalance(0);
        userRepository.save(user);
    }

    public void changeUserBalance(String userId, Integer tokenBalance) {
        UserEntity user = findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException();
        }
        user.setUserBalance(tokenBalance);
    }

    public List<UserEntity> readAll() {
        return userRepository.findAll();
    }

    public UserEntity read(UUID userId) {
        return userRepository.getById(userId);
    }

    public UserEntity findByUserId(String userId) {
        return userRepository.findByUserId(UUID.fromString(userId));
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserEntity findByEmailAndPassword(String email, String password) {
        UserEntity user = findByEmail(email);
        return user != null && passwordEncoder.matches(password, user.getPassword()) ? user : null;
    }

    /*public boolean update(UserEntity user, UUID userId) {
        if (userRepository.existsById(userId)) {
            user.setId(userId);
            create(user);
            return true;
        }
        return false;
    }*/

    public boolean delete(UUID userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }
}