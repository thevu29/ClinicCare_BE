package com.example.cliniccare.service;

import com.example.cliniccare.entity.RegistrationProcessRequest;
import com.example.cliniccare.dto.UserDTO;
import com.example.cliniccare.dto.UserFormDTO;
import com.example.cliniccare.entity.EmailVerification;
import com.example.cliniccare.entity.Role;
import com.example.cliniccare.entity.User;
import com.example.cliniccare.entity.UserInfoDetails;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.repository.EmailVerificationRepository;
import com.example.cliniccare.repository.RoleRepository;
import com.example.cliniccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@Lazy
public class AuthService implements UserDetailsService {
    private final EmailService emailService;
    private final FirebaseStorageService firebaseStorageService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    @Autowired
    public AuthService(
            EmailService emailService,
            FirebaseStorageService firebaseStorageService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmailVerificationRepository emailVerificationRepository
    ) {
        this.userRepository = userRepository;
        this.firebaseStorageService = firebaseStorageService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
        this.emailVerificationRepository = emailVerificationRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmailAndDeleteAtIsNull(username)
                .map(UserInfoDetails::new)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public User findOrCreateUserFromOAuth(String email, String name) {
        return userRepository.findByEmailAndDeleteAtIsNull(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setPassword(null);

                    Role userRole = roleRepository.findByNameIgnoreCase("user")
                            .orElseThrow(() -> new RuntimeException("Default role not found"));
                    newUser.setRole(userRole);

                    return userRepository.save(newUser);
                });
    }

    public void sendEmailOTP(RegistrationProcessRequest request) {
        if (userRepository.findByEmailAndDeleteAtIsNull(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }

        String otp = String.valueOf((int) (Math.random() * 900000) + 10000);

        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setEmail(request.getEmail());
        emailVerification.setOtp(otp);

        emailVerificationRepository.save(emailVerification);

        try {
            emailService.sendEmail(request.getEmail(), "ClinicCare Registration", "Your OTP is " + otp);
        } catch (RuntimeException e) {
            throw new BadRequestException("Failed to send email. The email address may not exist");
        }
    }

    public void verifyOtp(RegistrationProcessRequest request) {
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndOtpAndExpireAtAfter(request.getEmail(), request.getOtp(), LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        emailVerificationRepository.delete(verification);
    }

    public UserDTO registerUser(UserFormDTO userDTO) throws IOException {
        if (userRepository.findByEmailAndDeleteAtIsNull(userDTO.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(roleRepository.findByNameIgnoreCase("user")
                .orElseThrow(() -> new NotFoundException("Default role not found")));

        if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
            user.setImage(firebaseStorageService.uploadImage(userDTO.getImage()));
        }

        return new UserDTO(userRepository.save(user));
    }
}
