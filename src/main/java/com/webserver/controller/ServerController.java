package com.webserver.controller;

import com.webserver.server.Server;
import com.webserver.service.ServerManagerService;
import com.webserver.dto.LoadTestRequest;
import com.webserver.dto.LoadTestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ServerController {
    private final ServerManagerService serverManagerService;

    @PostMapping("/{serverType}/start")
    public ResponseEntity<Map<String, String>> startServer(
            @PathVariable String serverType,
            @RequestParam(defaultValue = "8080") int port) {
        try {
            serverManagerService.startServer(serverType, port);
            return ResponseEntity.ok(Map.of("message", "Server started successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{serverType}/stop")
    public ResponseEntity<Map<String, String>> stopServer(@PathVariable String serverType) {
        try {
            serverManagerService.stopServer(serverType);
            return ResponseEntity.ok(Map.of("message", "Server stopped successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{serverType}/status")
    public ResponseEntity<Map<String, Boolean>> getServerStatus(@PathVariable String serverType) {
        try {
            boolean isRunning = serverManagerService.isServerRunning(serverType);
            return ResponseEntity.ok(Map.of("running", isRunning));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("running", false));
        }
    }

    @GetMapping
    public ResponseEntity<List<String>> getAvailableServers() {
        List<String> serverTypes = serverManagerService.getAvailableServers()
            .stream()
            .map(Server::getServerType)
            .collect(Collectors.toList());
        return ResponseEntity.ok(serverTypes);
    }

    @PostMapping("/{serverType}/test")
    public ResponseEntity<LoadTestResult> runLoadTest(
            @PathVariable String serverType,
            @RequestBody LoadTestRequest request) {
        try {
            LoadTestResult result = serverManagerService.runLoadTest(
                serverType, 
                request.getPort(), 
                request.getNumberOfRequests()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
} 