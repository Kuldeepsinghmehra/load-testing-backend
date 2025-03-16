package com.webserver.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoadTestResult {
    private int totalRequests;
    private long totalTime;
    private double averageTime;
    private double requestsPerSecond;
    private double successRate;
} 