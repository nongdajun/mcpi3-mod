package com.nongdajun.mcpi3.conn;

import com.nongdajun.mcpi3.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MsgDispatcher implements Runnable {

    public static final Logger LOGGER = LoggerFactory.getLogger("pi3");
    private Socket socket;
    private boolean running;

    public MsgDispatcher(Socket socket){
        this.socket = socket;
        running = true;
    }

    @Override
    public void run() {

        InputStream inputStream;
        OutputStream outputStream;

        try {
            inputStream = this.socket.getInputStream();
            outputStream = this.socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            while (running) {
                int msg_len = inputStream.read();
                if(msg_len < 0){
                    LOGGER.warn("Pi3 client connection closed");
                    return;
                }
                if (msg_len > 127) {
                    msg_len &= 0x7F;
                    msg_len |= inputStream.read()<<7;
                    if(msg_len > 0x3FFF) {
                        msg_len &= 0x3FFF;
                        msg_len |= inputStream.read()<<14;
                        msg_len |= inputStream.read()<<22;
                    }
                }

                var bs = inputStream.readNBytes(msg_len);

                byte[] ret;

                try{
                    ret = executeCommand(bs);
                }
                catch (Exception exc){
                    LOGGER.error("executeCommand error: {}", exc);
                    ret = Constants.ERR_INTERNAL_ERROR;
                }

                if(ret == null || !Globals.echo){
                    outputStream.write(0);
                    continue;
                }

                int ret_len = ret.length;

                //LOGGER.info(String.format("SEND DATA SIZE: %d", ret_len));
                if(ret_len <= 127){
                    outputStream.write(ret_len);
                }
                else {
                    outputStream.write((ret_len & 0x7F)|0x80);
                    ret_len >>= 7;
                    if(ret_len <= 127){
                        outputStream.write(ret_len);
                    }
                    else {
                        outputStream.write((ret_len & 0x7F) | 0x80);
                        ret_len >>= 7;
                        outputStream.write(ret_len & 0xFF);
                        outputStream.write((ret_len>>8) & 0xFF);
                    }
                }

                outputStream.write(ret);
            }
        }catch (IOException e){
            LOGGER.error(String.format("连接已断开 => %s", e));
        } catch (Exception e) {
            LOGGER.error(String.format("Error processing the client command => %s", e));
            throw new RuntimeException(e);
        }

    }

    protected byte[] executeCommand(byte[] msg) {

        var args = ByteBuffer.wrap(msg).order(ByteOrder.LITTLE_ENDIAN);

        int cmd_code = args.getShort();

        switch (cmd_code&0xF000){
            case Commands.CommonCommandMask:
                return HandlerHub.commonHandler.execute(cmd_code, args);
            case Commands.ServerCommandMask:
                if(HandlerHub.serverHandler == null){
                    return Constants.ERR_SERVER_NOT_SUPPORT;
                }
                return HandlerHub.serverHandler.execute(cmd_code, args);
            case Commands.ClientCommandMask:
                if(HandlerHub.clientHandler == null){
                    return Constants.ERR_CLIENT_NOT_SUPPORT;
                }
                return HandlerHub.clientHandler.execute(cmd_code, args);
        }

        return Constants.ERR_UNKNOWN_COMMAND;
    }
}
