package com.caresync.controller;

import com.caresync.dto.request.CreateReviewRequest;
import com.caresync.dto.response.ReviewResponse;
import com.caresync.entity.User;
import com.caresync.security.SecurityUtil;
import com.caresync.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final SecurityUtil securityUtil;

    @PostMapping("/api/reviews")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(currentUser, request));
    }

    @GetMapping("/api/doctors/{doctorId}/reviews")
    public java.util.List<ReviewResponse> getReviewsForDoctor(@PathVariable Long doctorId) {
        return reviewService.getReviewsForDoctor(doctorId);
    }
}
