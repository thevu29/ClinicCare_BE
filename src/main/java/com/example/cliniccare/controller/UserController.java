package com.example.cliniccare.controller;

import com.example.cliniccare.exception.ResourceNotFoundException;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.UserRepository;
import com.example.cliniccare.utils.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@CrossOrigin("*")
@RestController
@RequestMapping("api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/all")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        List<Object> requiredFields = Arrays.asList(user.getEmail(), user.getPassword(), user.getPhone(),
                user.getName(), user.getRole());

        if (requiredFields.contains(null)) {
            throw new ResourceNotFoundException("Required fields are missing");
        }
        if (!Validation.isEmailValid(user.getEmail())) {
            throw new ResourceNotFoundException("Invalid email format");
        }
        if (!Validation.isPhoneValid(user.getPhone())) {
            throw new ResourceNotFoundException("Invalid phone number format");
        }

        userRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
            throw new ResourceNotFoundException("Email: " + user.getEmail() + " already used");
        });
        userRepository.findByPhone(user.getPhone()).ifPresent(existingUser -> {
            throw new ResourceNotFoundException("Phone number: " + user.getPhone() + " already used");
        });

        return userRepository.save(user);
    }
}
