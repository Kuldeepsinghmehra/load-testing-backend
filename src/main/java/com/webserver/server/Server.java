package com.webserver.server;

/**
 * Common interface for all server implementations.
 */
public interface Server {
    /**
     * Starts the server on the specified port
     * @param port The port number to listen on
     */
    void start(int port);

    /**
     * Stops the server
     */
    void stop();

    /**
     * @return The current status of the server
     */
    boolean isRunning();

    /**
     * @return The type of server implementation
     */
    String getServerType();
} 