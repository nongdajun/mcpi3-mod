package com.nongdajun.mcpi3.api;

import com.nongdajun.mcpi3.conn.CommandDispatcher;
import java.nio.ByteBuffer;

public class OptionHandler extends CommandHandler  {

    public OptionHandler(CommandDispatcher dispatcher){
        super(dispatcher);
    }

    public byte[] execute(int cmd_code, ByteBuffer args) {

        switch (cmd_code) {

            case Commands.Option.PING:
                return ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array();

            case Commands.Option.HELLO:
                return ByteBuffer.allocate(4).putInt(CommandDispatcher.VERSION).array();

            case Commands.Option.READY:
                if (GameInstances.player == null)
                    return new byte[]{0};
                else
                    return new byte[]{1};

            case Commands.Option.ECHO:
                dispatcher.echo = args.get() != 0;
                LOGGER.info(String.format("Echo is set to %s.", dispatcher.echo));
                break;

            case Commands.Option.ATTACH_PLAYER: {
                int player_id = args.getInt();
                String name = Utils.readArgString(args);
                for (var p : GameInstances.players) {
                    if (p.getId() == player_id || (player_id <= 0 && p.getName().getString().equals(name))) {
                        GameInstances.player = p;
                        LOGGER.info(String.format("Attached player %s", p.getName()));
                        return "OK".getBytes();
                    }
                }
            }
            break;

            case Commands.Option.CURRENT_PLAYER:
                if (GameInstances.player != null) {
                    return Utils.formatPlayerInfo(GameInstances.player).getBytes();
                }
                break;

            case Commands.Option.DEBUG: {
                return "NO IMPLEMENT".getBytes();
            }
        }
        return null;
    }

}
