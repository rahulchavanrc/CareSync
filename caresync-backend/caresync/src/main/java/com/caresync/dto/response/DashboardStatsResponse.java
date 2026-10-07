package com.caresync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalDoctors;
    private long totalPatients;
    private long totalAppointments;
    private long pendingCount;
    private long confirmedCount;
    private long completedCount;
    private long cancelledCount;
    private long totalReviews;
    private Double platformAverageRating;
}
