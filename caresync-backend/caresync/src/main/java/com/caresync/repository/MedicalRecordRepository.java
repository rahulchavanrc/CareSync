package com.caresync.repository;

import com.caresync.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatient_PatientIdOrderByCreatedAtDesc(Long patientId);
    Optional<MedicalRecord> findByAppointment_AppId(Long appId);
}
