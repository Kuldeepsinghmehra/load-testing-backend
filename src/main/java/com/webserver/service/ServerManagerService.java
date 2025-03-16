package com.webserver.service;

import com.webserver.server.Server;
import com.webserver.dto.LoadTestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class ServerManagerService {
    private final Map<String, Server> servers;
    private final List<Server> availableServers;

    public ServerManagerService(List<Server> servers) {
        this.availableServers = servers;
        this.servers = new ConcurrentHashMap<>();
        for (Server server : servers) {
            this.servers.put(server.getServerType(), server);
        }
    }

    public void startServer(String serverType, int port) {
        Server server = servers.get(serverType);
        if (server == null) {
            throw new IllegalArgumentException("Unknown server type: " + serverType);
        }
        if (server.isRunning()) {
            throw new IllegalStateException("Server is already running");
        }
        
        // Start the server in a new thread to not block
        new Thread(() -> {
            try {
                server.start(port);
            } catch (Exception e) {
                log.error("Error starting server: ", e);
            }
        }).start();
    }

    public void stopServer(String serverType) {
        Server server = servers.get(serverType);
        if (server == null) {
            throw new IllegalArgumentException("Unknown server type: " + serverType);
        }
        server.stop();
    }

    public boolean isServerRunning(String serverType) {
        Server server = servers.get(serverType);
        if (server == null) {
            throw new IllegalArgumentException("Unknown server type: " + serverType);
        }
        return server.isRunning();
    }

    public List<Server> getAvailableServers() {
        return availableServers;
    }

    public LoadTestResult runLoadTest(String serverType, int port, int numberOfRequests) {
        Server server = servers.get(serverType);
        if (server == null) {
            throw new IllegalArgumentException("Unknown server type: " + serverType);
        }
        if (!server.isRunning()) {
            throw new IllegalStateException("Server is not running");
        }

        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successfulRequests = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        String url = String.format("http://localhost:%d", port);

        // Submit all requests
        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    long requestStart = System.nanoTime();
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    long requestEnd = System.nanoTime();
                    
                    responseTimes.add((requestEnd - requestStart) / 1_000_000); // Convert to milliseconds
                    if (responseCode == 200) {
                        successfulRequests.incrementAndGet();
                    }
                } catch (IOException e) {
                    log.error("Error during load test: ", e);
                }
            });
        }

        // Shutdown executor and wait for completion
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Load test interrupted", e);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        double avgResponseTime = responseTimes.stream().mapToLong(Long::valueOf).average().orElse(0.0);
        
        return LoadTestResult.builder()
                .totalRequests(numberOfRequests)
                .totalTime(totalTime)
                .averageTime(avgResponseTime)
                .requestsPerSecond((double) numberOfRequests / (totalTime / 1000.0))
                .successRate((double) successfulRequests.get() / numberOfRequests * 100)
                .build();
    }
} 