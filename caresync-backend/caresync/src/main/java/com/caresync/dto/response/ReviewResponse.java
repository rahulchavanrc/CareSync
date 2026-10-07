package com.caresync.dto.response;

import com.caresync.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReviewResponse {
    private Long reviewId;
    private Long doctorId;
    private String reviewerDisplayName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review r) {
        return ReviewResponse.builder()
                .reviewId(r.getReviewId())
                .doctorId(r.getDoctor().getDoctorId())
                .reviewerDisplayName(toDisplayName(r.getPatient().getFullName()))
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }

    // "Jordan Lee" -> "Jordan L." — keeps reviews public without exposing a patient's full name.
    private static String toDisplayName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "Anonymous";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0];
        return parts[0] + " " + parts[parts.length - 1].charAt(0) + ".";
    }
}
