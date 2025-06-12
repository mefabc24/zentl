package org.example.demo3.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

// simple single client server => listens on a port for one client connection and handles message passing
public class Server implements Runnable {
    private final int port;
    private Consumer<String> onMessageReceived; // callback for incoming messages
    private volatile boolean running = true; // flag to control the main loop
    private PrintWriter out; // stream to send messages to the client
    private ServerSocket serverSocket;
    private final Runnable onStartupFailed; // callback for when the server cant start

    public Server(int port, Consumer<String> onMessageReceived, Runnable onStartupFailed) {
        this.port = port;
        this.onMessageReceived = onMessageReceived;
        this.onStartupFailed = onStartupFailed;
    }

    // main execution for the server thread
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("server started on port " + port + ". waiting for client");
            // waiting until a client connects
            Socket clientSocket = serverSocket.accept();
            System.out.println("client connected: " + clientSocket.getInetAddress());

            // setup input and output streams for traffic
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream())
            );

            // main loop to listen for messages
            while (running) {
                String message = in.readLine();
                if (message == null) {
                    // connection was closed by the client
                    break;
                }
                // callback with the received message
                onMessageReceived.accept(message);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("server error: " + e.getMessage());
                // failure callback
                    onStartupFailed.run();
            }
        } finally {
            stop();
        }
    }

    // message to the connected client
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // stop the server and close the socket
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("server stopped.");
            }
        } catch (IOException e) {
            System.err.println("error trying to stop server: " + e.getMessage());
        }
    }

    // allows changing the message handler dynamically
    // => needed to pass the server from one controller to another, from HostLobbyController to GameSetupController
    public void setOnMessageReceived(Consumer<String> handler) {
        this.onMessageReceived = handler;
    }
}