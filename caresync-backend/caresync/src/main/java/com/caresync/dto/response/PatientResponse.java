package com.caresync.dto.response;

import com.caresync.entity.Patient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class PatientResponse {
    private Long patientId;
    private String fullName;
    private String bloodGroup;
    private LocalDate dob;
    private String gender;

    public static PatientResponse from(Patient p) {
        return PatientResponse.builder()
                .patientId(p.getPatientId())
                .fullName(p.getFullName())
                .bloodGroup(p.getBloodGroup())
                .dob(p.getDob())
                .gender(p.getGender())
                .build();
    }
}
