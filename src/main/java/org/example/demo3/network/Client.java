package org.example.demo3.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

// connects to a server at a given ip and port and handls message passing
public class Client implements Runnable {
    private final String hostIp;
    private final int port;
    private Consumer<String> onMessageReceived; // callback for incoming messages
    private Runnable onConnectionFailed; // callback for  failed connection
    private volatile boolean running = true; // flag to control the main loop
    private PrintWriter out; // stream to send messages to the server
    private Socket socket;

    public Client(String hostIp, int port, Consumer<String> onMessageReceived, Runnable onConnectionFailed) {
        this.hostIp = hostIp;
        this.port = port;
        this.onMessageReceived = onMessageReceived;
        this.onConnectionFailed = onConnectionFailed;
    }

    // main execution for client thread
    @Override
    public void run() {
        try {
            // attempt to connect to the server
            socket = new Socket(hostIp, port);
            System.out.println("connected with server: " + hostIp);

            // setup input and output streams for comms
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // send an init msg to notify host
            out.println("client connected");

            // main loop => listens for messages from server
            while (running) {
                String message = in.readLine();
                if (message == null) {
                    // connection was closed by server
                    if (running && onConnectionFailed != null) {
                        onConnectionFailed.run();
                    }
                    break;
                }
                // callback with received message
                if (onMessageReceived != null) {
                    onMessageReceived.accept(message);
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("client error: " + e.getMessage());
                if (onConnectionFailed != null) {
                    onConnectionFailed.run();
                }
            }
        } finally {
            stop();
        }
    }

    // allows changing the message handler dynamically
    // => needed to pass the server from one controller to another, from HostLobbyController to GameSetupController
    public void setOnMessageReceived(Consumer<String> handler) {
        this.onMessageReceived = handler;
    }

    // sends a message to the connected server
    public void sendMessage(String message) {
        out.println(message);
    }

    public void setOnConnectionFailed(Runnable handler) {
        this.onConnectionFailed = handler;
    }

    // stops the client and closes the socket
    public void stop() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("client connection closed");
            }
        } catch (IOException e) {
            System.err.println("failed to close client: " + e.getMessage());
        }
    }
}