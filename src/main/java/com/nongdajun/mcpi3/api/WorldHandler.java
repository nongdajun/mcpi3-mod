package com.nongdajun.mcpi3.api;

import com.nongdajun.mcpi3.conn.CommandDispatcher;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;

public class WorldHandler extends CommandHandler  {

    public WorldHandler(CommandDispatcher dispatcher){
        super(dispatcher);
    }

    public byte[] execute(int cmd_code, ByteBuffer args){

        switch (cmd_code){

            case Commands.World.GET_BLOCK: {
                var block_state = GameInstances.world.getBlockState(new BlockPos(args.getInt(), args.getInt(), args.getInt()));
                if(block_state==null){
                    return null;
                }
                var block = block_state.getBlock();
                return Utils.formatBlockInfo(block).getBytes();
            }

            case Commands.World.SET_BLOCK: {
                var pos = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                String t = Utils.readArgString(args);
                var bt = Registries.BLOCK.get(Identifier.tryParse(t));
                if(bt!=null) {
                   if(GameInstances.world.setBlockState(pos, bt.getDefaultState())) {
                       return "OK".getBytes();
                   }
                }
            }
            break;

            case Commands.World.GET_BLOCK_WITH_DATA: {
                var block = GameInstances.world.getBlockState(new BlockPos(args.getInt(), args.getInt(), args.getInt()));
                if(block==null){
                    return null;
                }
                return block.toString().getBytes();
            }

            case Commands.World.GET_BLOCKS: {
                var pos0 = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                var pos1 = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                var blocks = GameInstances.world.getStatesInBoxIfLoaded(Box.enclosing(pos0, pos1));
                if(blocks==null){
                    return null;
                }
                ArrayList<String> ret = new ArrayList<>();
                for(var bs: blocks.toList()) {
                    var block = bs.getBlock();
                    ret.add(Utils.formatBlockInfo(block));
                }
                return String.join("|", ret).getBytes();
            }

            case Commands.World.SET_BLOCKS: {
                var x0 = args.getInt();
                var y0 = args.getInt();
                var z0 = args.getInt();
                var pos0 = new BlockPos(x0, y0, z0);
                var x1 = args.getInt();
                var y1 = args.getInt();
                var z1 = args.getInt();
                var pos1 = new BlockPos(x1, y1, z1);
                String t = Utils.readArgString(args);
                var bt = Registries.BLOCK.get(Identifier.tryParse(t));
                int counter = 0;
                if(bt!=null) {
                    var d_st = bt.getDefaultState();
                    var box = Box.enclosing(pos0, pos1);
                    for (int y = (int)box.minY; y < (int)box.maxY; ++y) {
                         for (int x = (int)box.minX; x < (int)box.maxX; ++x) {
                             for (int z = (int)box.minZ; z < (int)box.maxZ; ++z) {
                                if(GameInstances.world.setBlockState(new BlockPos(x, y, z), d_st)) {
                                    counter ++;
                                }
                            }
                        }
                    }
                    return String.valueOf(counter).getBytes();
                }
                break;
            }

            case Commands.World.GET_PLAYERS: {
                ArrayList<String> ret = new ArrayList<>();
                for (var player : GameInstances.players) {
                    ret.add(Utils.formatPlayerInfo(player));
                }
                return String.join("|", ret).getBytes();
            }

            case Commands.World.GET_BORDER: {
                var bdr = GameInstances.world.getWorldBorder();
                return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                        bdr.getCenterX(),
                        bdr.getCenterZ(),
                        bdr.getSize(),
                        bdr.getMaxRadius(),
                        bdr.getBoundNorth(),
                        bdr.getBoundEast(),
                        bdr.getBoundSouth(),
                        bdr.getBoundWest()
                ).getBytes();
            }

            case Commands.World.GET_ENTITY_TYPES: {
                ArrayList<String> ret = new ArrayList<>();
                for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
                    ret.add(String.format("%s,%s", entityType.getUntranslatedName(), (entityType.isSummonable()?"1":"0")));
                }
                return String.join("|", ret).getBytes();
            }

            case Commands.World.GET_ENTITY: {
                var entity = GameInstances.world.getEntityById(args.getInt());
                if(entity==null){
                    return null;
                }
                return Utils.formatEntityInfo(entity).getBytes();
            }

            case Commands.World.GET_ENTITY_BY_TYPE: {
                short n = args.getShort();
                ArrayList<Optional<EntityType<?>>> type_arr = new ArrayList<>();
                for(short i=0; i<n; i++){
                    String s = Utils.readArgString(args);
                    var t = EntityType.get(s);
                    type_arr.add(t);
                }
                if (type_arr.size() == 0) {
                    return new byte[]{};
                }

                double max = args.getDouble();

                if (max <= 0.0) {
                    max = GameInstances.world.getWorldBorder().getSize();
                }

                ArrayList<String> ret = new ArrayList<>();
                for (var t : type_arr) {
                    var entities = GameInstances.world.getEntitiesByType(t.get(), Box.of(GameInstances.player.getPos(), max, max, max), m -> true);
                    if (entities == null) {
                        continue;
                    }
                    for (Entity entity : entities) {
                        ret.add(Utils.formatEntityInfo(entity));
                    }
                }
                return String.join("|", ret).getBytes();
            }

            case Commands.World.SPAWN_ENTITY:{
                String type_name = Utils.readArgString(args);
                double x = args.getDouble();
                double y = args.getDouble();
                double z = args.getDouble();
                String custom_name = Utils.readArgString(args);
                var entity = EntityType.get(type_name).get().create(GameInstances.world);
                entity.setPos(x, y, z);
                entity.setCustomName(Text.of(custom_name));
                if(GameInstances.world.spawnEntity(entity)) {
                    return String.valueOf(entity.getId()).getBytes();
                }
                else{
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    return "-1".getBytes();
                }
            }

            case Commands.World.REMOVE_ENTITY:{
                var entity = GameInstances.world.getEntityById(args.getInt());
                if(entity==null){
                    return null;
                }
                entity.remove(Entity.RemovalReason.DISCARDED);
                return "OK".getBytes();
            }

            case Commands.World.REMOVE_ENTITY_BY_TYPE:{
                int count = 0;
                short n = args.getShort();
                ArrayList<Optional<EntityType<?>>> type_arr = new ArrayList<>();
                for(short i=0; i<n; i++){
                    String s = Utils.readArgString(args);
                    var t = EntityType.get(s);
                    type_arr.add(t);
                }
                if (type_arr.size() == 0) {
                    return String.valueOf(count).getBytes();
                }

                double max = args.getDouble();

                if (max <= 0.0) {
                    max = GameInstances.world.getWorldBorder().getSize();
                }

                ArrayList<String> ret = new ArrayList<>();
                for (var t : type_arr) {
                    var entities = GameInstances.world.getEntitiesByType(t.get(), Box.of(GameInstances.player.getPos(), max, max, max), m -> true);
                    if (entities == null) {
                        continue;
                    }
                    for (Entity entity : entities) {
                        count ++;
                        entity.remove(Entity.RemovalReason.DISCARDED);
                    }
                }

                return String.valueOf(count).getBytes();
            }

            case Commands.World.GET_BLOCK_TYPES: {
                ArrayList<String> ret = new ArrayList<>();
                for (var b : Registries.BLOCK) {
                    ret.add(Utils.formatBlockInfo(b));
                }
                return String.join("|", ret).getBytes();
            }
        }

        return null;
    }

}
