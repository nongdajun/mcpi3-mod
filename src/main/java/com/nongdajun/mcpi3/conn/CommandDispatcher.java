package com.nongdajun.mcpi3.conn;

import com.nongdajun.mcpi3.Pi3;
import com.nongdajun.mcpi3.api.GameInstances;
import com.nongdajun.mcpi3.api.OptionHandler;
import com.nongdajun.mcpi3.api.MiscHandler;
import com.nongdajun.mcpi3.api.WorldHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

public class CommandDispatcher implements Runnable {

    public static final Logger LOGGER = LoggerFactory.getLogger("pi3");
    private Socket socket;
    private boolean running;

    public static final int VERSION = 1;

    public boolean echo = true;

    private OptionHandler optionHandler = new OptionHandler(this);
    private WorldHandler worldHandler = new WorldHandler(this);
    private MiscHandler miscHandler = new MiscHandler(this);

    public CommandDispatcher(Socket socket){
        this.socket = socket;
        running = true;
    }

    @Override
    public void run() {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = this.socket.getInputStream();
            outputStream = this.socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            while (running) {
                int msg_len = inputStream.read();
                if(msg_len<0){
                    LOGGER.warn("Pi3 client connection closed");
                    return;
                }
                if (msg_len>127) {
                    msg_len &= 0x7F;
                    msg_len |= inputStream.read()<<7;
                    if(msg_len>0x3FFF) {
                        msg_len &= 0x3FFF;
                        msg_len |= inputStream.read()<<14;
                        msg_len |= inputStream.read()<<22;
                    }
                }

                var call_id = inputStream.readNBytes(3);
                var bs = inputStream.readNBytes(msg_len - 3);

                byte[] ret = null;

                try{
                    ret = executeCommand(bs);
                }
                catch (Exception exc){
                    var s = "ERROR:"+exc.getMessage();
                    LOGGER.error(exc.toString());
                    ret = s.getBytes();
                }

                if(!echo){
                    return;
                }

                int ret_len = 3;
                if (ret != null) {
                    ret_len += ret.length;
                }
                LOGGER.info(String.format("SEND INFO: %d", ret_len));
                if(ret_len<=127){
                    outputStream.write(ret_len);
                }
                else {
                    outputStream.write((ret_len & 0x7F)|0x80);
                    ret_len >>= 7;
                    if(ret_len<=127){
                        outputStream.write(ret_len);
                    }
                    else {
                        outputStream.write((ret_len & 0x7F) | 0x80);
                        ret_len >>= 7;
                        outputStream.write(ret_len & 0xFF);
                        outputStream.write((ret_len>>8) & 0xFF);
                    }
                }
                outputStream.write(call_id);
                if(ret != null) {
                    outputStream.write(ret);
                }
            }
        }catch (IOException e){
            LOGGER.error(String.format("连接已断开 => %s", e));
        } catch (Exception e) {
            LOGGER.error(String.format("Error processing the client command => %s", e));
            throw new RuntimeException(e);
        }

    }

    private byte[] executeCommand(byte[] msg) {

        var args = ByteBuffer.wrap(msg);
        args.order(ByteOrder.LITTLE_ENDIAN);
        int cmd_code = args.getShort();

        if (cmd_code >= 0x100 && GameInstances.world == null) {
            return "ERROR:NOT_READY".getBytes();
        }

        switch (cmd_code&0xFF00){

            case 0x0:
                return optionHandler.execute(cmd_code, args);

            case 0x100:
                return worldHandler.execute(cmd_code, args);

            case 0x200:
                if (GameInstances.player == null) {
                    return "ERROR:PLAYER_NOT_READY".getBytes();
                }
                return null;

            case 0x300:
                return null;

            case 0x400:
                return null;

            case 0x500:
                return miscHandler.execute(cmd_code, args);

            case 0x600:
                return null;

        }

        return null;
    }
}
