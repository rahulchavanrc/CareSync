package com.caresync.service;

import com.caresync.dto.request.DoctorCreateRequest;
import com.caresync.dto.request.LoginRequest;
import com.caresync.dto.request.RegisterRequest;
import com.caresync.dto.response.AuthResponse;
import com.caresync.entity.Doctor;
import com.caresync.entity.Patient;
import com.caresync.entity.Role;
import com.caresync.entity.User;
import com.caresync.exception.DuplicateResourceException;
import com.caresync.repository.DoctorRepository;
import com.caresync.repository.PatientRepository;
import com.caresync.repository.UserRepository;
import com.caresync.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse registerPatient(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_PATIENT)
                .build();
        user = userRepository.save(user);

        Patient patient = Patient.builder()
                .user(user)
                .fullName(request.getFullName())
                .bloodGroup(request.getBloodGroup())
                .dob(request.getDob())
                .gender(request.getGender())
                .build();
        patient = patientRepository.save(patient);

        log.info("Registered new patient '{}' (userId={}, patientId={})",
                user.getEmail(), user.getUserId(), patient.getPatientId());

        String token = jwtUtil.generateToken(user, user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole(), patient.getPatientId());
    }

    @Transactional
    public AuthResponse createDoctor(DoctorCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_DOCTOR)
                .build();
        user = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .fullName(request.getFullName())
                .specialization(request.getSpecialization())
                .experienceYears(request.getExperienceYears())
                .consultationFee(request.getConsultationFee())
                .bio(request.getBio())
                .photoUrl(request.getPhotoUrl())
                .build();
        doctor = doctorRepository.save(doctor);

        log.info("Admin onboarded new doctor '{}' (userId={}, doctorId={}, specialization={})",
                user.getEmail(), user.getUserId(), doctor.getDoctorId(), doctor.getSpecialization());

        String token = jwtUtil.generateToken(user, user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole(), doctor.getDoctorId());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User authenticated but not found: " + request.getEmail()));

        Long profileId = switch (user.getRole()) {
            case ROLE_DOCTOR -> doctorRepository.findByUser_UserId(user.getUserId())
                    .map(Doctor::getDoctorId).orElse(null);
            case ROLE_PATIENT -> patientRepository.findByUser_UserId(user.getUserId())
                    .map(Patient::getPatientId).orElse(null);
            case ROLE_ADMIN -> null;
        };

        log.info("User '{}' logged in with role {}", user.getEmail(), user.getRole());

        String token = jwtUtil.generateToken(user, user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole(), profileId);
    }
}
