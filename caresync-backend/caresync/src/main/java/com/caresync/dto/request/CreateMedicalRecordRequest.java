package com.caresync.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMedicalRecordRequest {

    @NotNull
    private Long appointmentId;

    private String symptoms;

    @NotBlank
    private String diagnosis;

    /** Free-text list of prescribed medicines, e.g. "Amoxicillin 500mg - 3x/day for 7 days". */
    private String prescription;
}
