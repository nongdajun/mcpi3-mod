package com.nongdajun.mcpi3.api.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;


public abstract class CommandHandler implements ICommandHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger("pi3_handler");

    public CommandHandler(){

    }

    public abstract byte[] execute(int cmd_code, ByteBuffer args);

}
