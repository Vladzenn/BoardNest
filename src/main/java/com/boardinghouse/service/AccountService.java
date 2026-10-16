package com.boardinghouse.service;

import com.boardinghouse.model.User;
import com.boardinghouse.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    public AccountService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users; this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public void register(String fullName, String username, String password) {
        if (fullName == null || fullName.isBlank() || fullName.trim().length() > 100)
            throw new IllegalArgumentException("Enter your name (up to 100 characters).");
        if (username == null || !username.trim().matches("[A-Za-z0-9._-]{3,50}"))
            throw new IllegalArgumentException("Username must be 3–50 characters using letters, numbers, dots, underscores, or hyphens.");
        String normalized = username.trim().toLowerCase();
        if (users.existsByUsername(normalized)) throw new IllegalArgumentException("That username is already in use.");
        if (password == null || password.length() < 8) throw new IllegalArgumentException("Use a password with at least 8 characters.");
        users.save(new User(fullName.trim(), normalized, passwordEncoder.encode(password)));
    }
}
