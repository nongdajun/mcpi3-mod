package com.nongdajun.mcpi3.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class Utils {

    public static String readArgString(ByteBuffer buf){
        int length = buf.getShort();
        var bs = new byte[length];
        buf.get(bs, 0, length);
        return new String(bs);
    }

    public static String Text2String(Text text){
        if(text == null){
            return "";
        }
        else{
            return text.getString();
        }
    }

    public static String formatEntityInfo(Entity entity){
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s"
                ,entity.getId()
                ,entity.getType().getUntranslatedName()
                ,entity.getX()
                ,entity.getY()
                ,entity.getZ()
                ,Utils.Text2String(entity.getCustomName())
                ,(entity.isAlive()?"1":"0")
                ,(entity.isOnGround()?"1":"0")
        );
    }

    public static String formatPlayerInfo(PlayerEntity player){
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s"
                ,player.getId()
                ,Utils.Text2String(player.getName())
                ,player.getX()
                ,player.getY()
                ,player.getZ()
                ,(player.isMainPlayer()?"1":"0")
                ,(player.isAlive()?"1":"0")
                ,player.getHealth()
        );
    }

    public static String formatBlockInfo(BlockState blockState){
        return blockState.getRegistryEntry().getKey().get().getValue().getPath();
    }

    public static String formatRegEntrySetInfo(Registry<?> reg) {
        ArrayList<String> ret = new ArrayList<>();
        for (var b : reg.getEntrySet()) {
            ret.add(b.getKey().getValue().getPath());
        }
        return String.join("|", ret);
	}
}
