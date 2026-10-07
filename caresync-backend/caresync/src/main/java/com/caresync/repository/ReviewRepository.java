package com.caresync.repository;

import com.caresync.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByDoctor_DoctorIdOrderByCreatedAtDesc(Long doctorId);

    boolean existsByAppointment_AppId(Long appId);

    long countByDoctor_DoctorId(Long doctorId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.doctorId = :doctorId")
    Optional<Double> findAverageRatingByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT AVG(r.rating) FROM Review r")
    Optional<Double> findOverallAverageRating();

    @Query("SELECT r.appointment.appId FROM Review r WHERE r.patient.patientId = :patientId")
    List<Long> findReviewedAppointmentIdsByPatientId(@Param("patientId") Long patientId);
}
