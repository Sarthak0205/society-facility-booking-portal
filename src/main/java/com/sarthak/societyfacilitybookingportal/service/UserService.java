package com.sarthak.societyfacilitybookingportal.service;

import com.sarthak.societyfacilitybookingportal.entity.User;
import com.sarthak.societyfacilitybookingportal.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String registerUser(String fullName, String email, String password, String confirmPassword) {

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
        user.setPassword(password);

        userRepository.save(user);

        return "Registration successful!";
    }
}