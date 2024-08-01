package com.nongdajun.mcpi3.api.handlers;

import com.nongdajun.mcpi3.api.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class CommonHandler extends CommandHandler {

    public byte[] execute(int cmd_code, ByteBuffer args) {

        Commands.Common op;
        try{
            op = Commands.codeToCommon(cmd_code);
        }
        catch (Exception ex){
            return Constants.ERR_UNKNOWN_COMMAND;
        }

        switch (op) {

            case PING: {
                return ByteBuffer.allocate(8)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putLong(System.currentTimeMillis()).array();
            }

            case GET_API_VERSION: {
                return ByteBuffer.allocate(4)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(Config.VERSION).array();
            }

            case IS_SERVER_READY: {
                if (HandlerHub.serverHandler == null || Globals.server==null)
                    return Constants.N;
                else
                    return Constants.Y;
            }

            case IS_CLIENT_READY: {
                if (HandlerHub.clientHandler == null)
                    return Constants.N;
                else
                    return Constants.Y;
            }

            case SET_ECHO_MODE: {
                var v = args.get();
                Globals.echo = (v != 0 && v != Constants.N[0]);
                LOGGER.info(String.format("Echo is set to %s.", Globals.echo));
                break;
            }

            case DEBUG: {
                return Constants.ERR_NOT_IMPLEMENT;
            }
        }
        return null;
    }

}
