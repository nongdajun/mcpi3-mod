package com.nongdajun.mcpi3.api;

import com.nongdajun.mcpi3.conn.CommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;


public abstract class CommandHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger("pi3");
    protected CommandDispatcher dispatcher;

    public CommandHandler(CommandDispatcher dispatcher){
        this.dispatcher = dispatcher;
    }

    public abstract byte[] execute(int cmd_code, ByteBuffer args);


}
