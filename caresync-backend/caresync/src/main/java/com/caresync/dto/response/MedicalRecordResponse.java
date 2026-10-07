package com.caresync.dto.response;

import com.caresync.entity.MedicalRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MedicalRecordResponse {
    private Long recordId;
    private Long appointmentId;
    private String doctorName;
    private String patientName;
    private String symptoms;
    private String diagnosis;
    private String prescription;
    private LocalDateTime createdAt;

    public static MedicalRecordResponse from(MedicalRecord r) {
        return MedicalRecordResponse.builder()
                .recordId(r.getRecordId())
                .appointmentId(r.getAppointment().getAppId())
                .doctorName(r.getDoctor().getFullName())
                .patientName(r.getPatient().getFullName())
                .symptoms(r.getSymptoms())
                .diagnosis(r.getDiagnosis())
                .prescription(r.getPrescription())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
