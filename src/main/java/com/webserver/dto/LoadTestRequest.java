package com.webserver.dto;

import lombok.Data;

@Data
public class LoadTestRequest {
    private int port;
    private int numberOfRequests;
} 