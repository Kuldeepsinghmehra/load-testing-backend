package com.webserver.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class ThreadPoolServer implements Server {
    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService executorService;
    private static final int THREAD_POOL_SIZE = 10;

    public ThreadPoolServer() {
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    @Override
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            log.info("Thread pool server started on port {} with {} threads", port, THREAD_POOL_SIZE);
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            log.error("Error in thread pool server: ", e);
            stop();
        }
    }

    @Override
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            executorService.shutdown();
        } catch (IOException e) {
            log.error("Error closing server socket: ", e);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public String getServerType() {
        return "Thread-Pool";
    }

    private void handleClient(Socket clientSocket) {
        try {
            // Read the request (not used in this simple implementation)
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (!in.readLine().isEmpty()) {
                // Skip request headers
            }

            // Send response
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/plain");
            out.println("Content-Length: 2");
            out.println();
            out.println("OK");
            
            // Close streams and socket
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            log.error("Error handling client: ", e);
        }
    }
} 