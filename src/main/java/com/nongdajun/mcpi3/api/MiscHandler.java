package com.nongdajun.mcpi3.api;
import com.mojang.brigadier.ParseResults;
import com.nongdajun.mcpi3.Pi3;
import com.nongdajun.mcpi3.conn.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.nio.ByteBuffer;

public class MiscHandler extends CommandHandler {

    public MiscHandler(CommandDispatcher dispatcher){
        super(dispatcher);
    }

    public byte[] execute(int cmd_code, ByteBuffer args){

        switch (cmd_code){

            case Commands.Misc.CHAT:
                chat(Utils.readArgString(args));
                return null;

            case Commands.Misc.COMMAND:

                command(Utils.readArgString(args));
                return null;

        }

        return null;
    }

    private void chat(String msg){
        if(GameInstances.player!=null) {
            GameInstances.player.sendMessage(Text.of(msg));
        }
    }

    private void command(String msg){
        var src = GameInstances.player.getCommandSource();
        GameInstances.server.getCommandManager().executeWithPrefix(src, msg);
    }
}
