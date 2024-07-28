package com.nongdajun.mcpi3.conn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Connection implements Runnable {

   public static final Logger LOGGER = LoggerFactory.getLogger("pi3");

    private String host;
    private int port;
    private ServerSocket server;
    private Thread thread;
    private boolean running;

    public Connection(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void init() {

        try {
            server = new ServerSocket(this.port);
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to create server socket, port = %d .", this.port));
            throw new RuntimeException(e);
        }

        thread = new Thread(this);
        running = true;
        thread.start();
    }

    public void run() {
        LOGGER.info("Pi3 server thread started!");
        while (running) {
            try {
                Socket socket = server.accept();
                new Thread(new CommandDispatcher(socket)).start();
                LOGGER.info(String.format("* New connection from %s ", socket.getRemoteSocketAddress().toString()));
            } catch (IOException e) {
                LOGGER.error("Failed to accept connection.");
                throw new RuntimeException(e);
            }
        }
        LOGGER.warn("Pi3 server thread exited!");
    }

}
