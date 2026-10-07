package com.caresync.dto.response;

import com.caresync.entity.Doctor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class DoctorResponse {
    private Long doctorId;
    private String fullName;
    private String specialization;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String bio;
    private String photoUrl;
    private Double averageRating;
    private Long reviewCount;

    /** Basic mapping without rating stats — used where review data hasn't been fetched. */
    public static DoctorResponse from(Doctor d) {
        return from(d, null, 0L);
    }

    public static DoctorResponse from(Doctor d, Double averageRating, Long reviewCount) {
        return DoctorResponse.builder()
                .doctorId(d.getDoctorId())
                .fullName(d.getFullName())
                .specialization(d.getSpecialization())
                .experienceYears(d.getExperienceYears())
                .consultationFee(d.getConsultationFee())
                .bio(d.getBio())
                .photoUrl(d.getPhotoUrl())
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .build();
    }
}
