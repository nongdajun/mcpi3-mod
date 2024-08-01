package com.nongdajun.mcpi3.debug;

import com.nongdajun.mcpi3.Pi3;
import com.nongdajun.mcpi3.api.Commands;
import com.nongdajun.mcpi3.conn.MsgDispatcher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestHelper {

    static class CommandDispatcherDbg extends MsgDispatcher {
        public CommandDispatcherDbg() {
            super(null);
        }

        public byte[] executeCommand(byte[] msg) {
            return super.executeCommand(msg);
        }
    }

    public static class Request{
        public final ByteBuffer args = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN);
        public Request(int cmd_code){
            assert cmd_code<=0x7fff;
            args.putShort((short) cmd_code);
        }
        public Request(Commands.Common cmd){
            args.putShort((short)(Commands.CommonCommandMask|cmd.ordinal()));
        }
        public Request(Commands.Server cmd){
            args.putShort((short)(Commands.ServerCommandMask|cmd.ordinal()));
        }
        public Request(Commands.Client cmd){
            args.putShort((short)(Commands.ClientCommandMask|cmd.ordinal()));
        }

        public void putStr(String str){
            var bs = str.getBytes();
            assert bs.length <= 65537;
            args.putShort((short)bs.length);
            args.put(bs);
        }

        public byte[] data(){
            return args.array();
        }
    }

    public static void run(){

        CommandDispatcherDbg dispatcher = new CommandDispatcherDbg();

        var req = new Request(Commands.Client.PLAYER_MOVE_TO);
        req.args.putFloat(2.0f);
        req.args.putFloat(0f);
        req.args.putFloat(0f);
        req.args.putFloat(0f);

        //req.args.putDouble(30.508320146547224);
        //req.args.putDouble(88.0);
        //req.args.putDouble(-62.69999998807907);
        //req.putStr("golden_sword");

        var ret = dispatcher.executeCommand(req.data());

        if(ret!=null) {
            Pi3.LOGGER.info("DEBUG_RET: {}", new String(ret));
        }
        else{
            Pi3.LOGGER.warn("DEBUG_RET: null");
        }


    }


}
