package com.example.photo_kedr.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.photo_kedr.model.User;
import com.example.photo_kedr.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {
    private static final int DEFAULT_PRIVILEGES = 1;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserProfile register(String login, String rawPassword) {
        String normalizedLogin = login == null ? "" : login.trim();
        if (normalizedLogin.isBlank()) {
            throw new IllegalArgumentException("Укажите имя пользователя.");
        }
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("Пароль должен содержать не менее 8 символов.");
        }
        if (userRepository.findByLogin(normalizedLogin).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует.");
        }

        User user = new User(0, normalizedLogin, passwordEncoder.encode(rawPassword), DEFAULT_PRIVILEGES);
        try {
            userRepository.add(user);
        } catch (org.springframework.dao.DuplicateKeyException exception) {
            // The unique DB constraint handles two concurrent registration requests safely.
            throw new IllegalArgumentException("Пользователь с таким именем уже существует.", exception);
        }
        return new UserProfile(user.getId(), user.getLogin());
    }

    public UserProfile getProfile(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new UserProfile(user.getId(), user.getLogin());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getLogin())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public record UserProfile(int id, String login) {
    }
}
