package com.nongdajun.mcpi3.api.handlers;

import java.nio.ByteBuffer;

public interface ICommandHandler {

    public byte[] execute(int cmd_code, ByteBuffer args);

}
