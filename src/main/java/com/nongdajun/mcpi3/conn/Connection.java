package com.nongdajun.mcpi3.conn;
import com.nongdajun.mcpi3.api.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Connection implements Runnable {

    public static final Logger LOGGER = LoggerFactory.getLogger("pi3");

    private String host;
    private int port;
    private ServerSocket server;
    private Thread thread;
    private boolean running;

    private static Connection _instance;

    public Connection(String host, int port){
        this.host = host;
        this.port = port;
    }

    public static final void init() {

        if(_instance!=null){
            return;
        }

        _instance = new Connection(Config.getHost(), Config.PORT);

        try {
            _instance.server = new ServerSocket(Config.PORT, 0, InetAddress.getByName (_instance.host));
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to create server socket, port = %d .", _instance.port));
            throw new RuntimeException(e);
        }

        _instance.thread = new Thread(_instance);
        _instance.running = true;
        _instance.thread.start();
    }

    public void run() {
        LOGGER.info("Pi3 server thread started!");
        while (running) {
            try {
                Socket socket = server.accept();
                new Thread(new MsgDispatcher(socket)).start();
                LOGGER.info(String.format("* New connection from %s *", socket.getRemoteSocketAddress().toString()));
            } catch (IOException e) {
                LOGGER.error("Failed to accept connection.");
                throw new RuntimeException(e);
            }
        }
        LOGGER.warn("Pi3 server thread exited!");
    }

}
