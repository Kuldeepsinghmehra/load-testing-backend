package com.webserver.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
@Component
public class SingleThreadedServer implements Server {
    private ServerSocket serverSocket;
    private boolean running;

    @Override
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            log.info("Single-threaded server started on port {}", port);
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            log.error("Error in single-threaded server: ", e);
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
        return "Single-Threaded";
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