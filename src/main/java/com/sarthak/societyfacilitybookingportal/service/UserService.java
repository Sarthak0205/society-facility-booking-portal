package com.sarthak.societyfacilitybookingportal.service;

import com.sarthak.societyfacilitybookingportal.entity.User;
import com.sarthak.societyfacilitybookingportal.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String registerUser(
            String fullName,
            String email,
            String password,
            String confirmPassword) {

        if (fullName == null || fullName.isBlank()) {
            return "Full name is required.";
        }

        if (email == null || email.isBlank()) {
            return "Email is required.";
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return "Please enter a valid email address.";
        }

        if (password == null || password.isBlank()) {
            return "Password is required.";
        }

        if (password.length() < 6) {
            return "Password must be at least 6 characters.";
        }

        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return "An account with this email already exists.";
        }

        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);

        // Store only the BCrypt hash, never the plain-text password.
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);

        return "Registration successful!";
    }

    public User authenticateUser(String email, String password) {

        if (email == null || email.isBlank()) {
            return null;
        }

        if (password == null || password.isBlank()) {
            return null;
        }

        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(
                        password,
                        user.getPassword()
                ))
                .orElse(null);
    }
}