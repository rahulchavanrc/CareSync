package com.caresync.repository;

import com.caresync.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser_UserId(Long userId);
    List<Doctor> findBySpecializationIgnoreCaseContaining(String specialization);
}
