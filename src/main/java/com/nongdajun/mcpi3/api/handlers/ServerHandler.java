package com.nongdajun.mcpi3.api.handlers;

import com.nongdajun.mcpi3.api.Commands;
import com.nongdajun.mcpi3.api.Constants;
import com.nongdajun.mcpi3.api.Globals;
import com.nongdajun.mcpi3.api.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Optional;

public class ServerHandler extends CommandHandler {

    public byte[] execute(int cmd_code, ByteBuffer args){

        if(Globals.server == null){
            return Constants.ERR_SERVER_NOT_READY;
        }

        Commands.Server op;
        try{
            op = Commands.codeToServer(cmd_code);
        }
        catch (Exception ex){
            return Constants.ERR_UNKNOWN_COMMAND;
        }

        if(op.compareTo(Commands.Server.__WORLD_COMMANDS_START__)>=0
        && op.compareTo(Commands.Server.__WORLD_COMMANDS_END__)<=0
        && Globals.world == null){
            return Constants.ERR_WORLD_NOT_ATTACHED;
        }

        if(op.compareTo(Commands.Server.__PLAYER_COMMANDS_START__)>=0
        && op.compareTo(Commands.Server.__PLAYER_COMMANDS_END__)<=0
        && Globals.player == null){
            return Constants.ERR_PLAYER_NOT_ATTACHED;
        }

        switch (op){

            case GET_PLAYERS: {
                ArrayList<String> ret = new ArrayList<>();
                for (var p : Globals.server.getPlayerManager().getPlayerList()) {
                    ret.add(Utils.formatPlayerInfo(p));
                }
                return String.join("|", ret).getBytes();
            }

            case ATTACH_PLAYER: {
                String name = Utils.readArgString(args);
                int player_id = args.getInt();
                ServerPlayerEntity player = tryGetPlayer(name, player_id);
                if(player != null) {
                    Globals.player = player;
                    LOGGER.info(String.format("Attached player %s", Utils.Text2String(player.getName())));
                    return Constants.OK;
                }
                else{
                    return Constants.FAILED;
                }
            }

            case CURRENT_PLAYER: {
                if (Globals.player != null) {
                    return Utils.formatPlayerInfo(Globals.player).getBytes();
                }
                break;
            }

            case GET_WORLDS: {
                ArrayList<String> ret = new ArrayList<>();
                for (var w: Globals.server.getWorlds()) {
                    ret.add(w.getRegistryKey().getValue().getPath());
                }
                return String.join("|", ret).getBytes();
            }

            case ATTACH_WORLD: {
                String name = Utils.readArgString(args);
                if(!name.isEmpty()){
                    for (var w: Globals.server.getWorlds()) {
                        if(name.equals(w.getRegistryKey().getValue().getPath())){
                            Globals.world = w;
                            return Constants.OK;
                        }
                    }
                }
                else{
                    if(Globals.player != null){
                        Globals.world = Globals.player.getWorld();
                        return Constants.OK;
                    }
                }
                break;
            }

            case CURRENT_WORLD: {
                if (Globals.world != null) {
                    return Globals.world.getRegistryKey().getValue().getPath().getBytes();
                }
                break;
            }

            case KILL_PLAYER:{
                String name = Utils.readArgString(args);
                int player_id = args.getInt();
                ServerPlayerEntity player = tryGetPlayer(name, player_id);
                if(player!=null) {
                    player.kill();
                    return Constants.OK;
                }
                break;
            }

            case GET_GAME_MODE:{
                var mode = Globals.server.getForcedGameMode();
                if(mode == null){
                    return null;
                }
                else{
                    return mode.getName().getBytes();
                }
            }

            case GET_GAME_VERSION:{
                return Globals.server.getVersion().getBytes();
            }

            case WORLD_GET_NAME:{
                if(Globals.world!=null) {
                    return Globals.world.getRegistryKey().getValue().getPath().getBytes();
                }
            }

            case WORLD_GET_BLOCK: {
                var block_state = Globals.world.getBlockState(new BlockPos(args.getInt(), args.getInt(), args.getInt()));
                if(block_state==null){
                    return null;
                }
                return Utils.formatBlockInfo(block_state).getBytes();
            }

            case WORLD_SET_BLOCK: {
                var pos = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                String t = Utils.readArgString(args);
                var bt = Globals.server.getRegistryManager().get(RegistryKeys.BLOCK).get(Identifier.tryParse(t));
                if(bt!=null) {
                   if(Globals.world.setBlockState(pos, bt.getDefaultState())) {
                       return Constants.OK;
                   }
                }
                break;
            }

            case WORLD_GET_BLOCK_WITH_DATA: {
                var block = Globals.world.getBlockState(new BlockPos(args.getInt(), args.getInt(), args.getInt()));
                if(block==null){
                    return null;
                }
                return block.toString().getBytes();
            }

            case WORLD_GET_BLOCKS: {
                var pos0 = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                var pos1 = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                var blocks = Globals.world.getStatesInBoxIfLoaded(Box.enclosing(pos0, pos1));
                if(blocks==null){
                    return null;
                }
                ArrayList<String> ret = new ArrayList<>();
                for(var bs: blocks.toList()) {
                    ret.add(Utils.formatBlockInfo(bs));
                }
                return String.join("|", ret).getBytes();
            }

            case WORLD_SET_BLOCKS: {
                var pos0 = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                var pos1 = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                String t = Utils.readArgString(args);
                var bt = Globals.server.getRegistryManager().get(RegistryKeys.BLOCK).get(Identifier.tryParse(t));
                int counter = 0;
                if(bt!=null) {
                    var d_st = bt.getDefaultState();
                    var box = Box.enclosing(pos0, pos1);
                    for (int y = (int)box.minY; y < (int)box.maxY; ++y) {
                         for (int x = (int)box.minX; x < (int)box.maxX; ++x) {
                             for (int z = (int)box.minZ; z < (int)box.maxZ; ++z) {
                                if(Globals.world.setBlockState(new BlockPos(x, y, z), d_st)) {
                                    counter ++;
                                }
                            }
                        }
                    }
                    return String.valueOf(counter).getBytes();
                }
                break;
            }

            case WORLD_GET_PLAYERS: {
                ArrayList<String> ret = new ArrayList<>();
                for (var player : Globals.world.getPlayers()) {
                    ret.add(Utils.formatPlayerInfo(player));
                }
                return String.join("|", ret).getBytes();
            }

            case WORLD_GET_BORDER: {
                var bdr = Globals.world.getWorldBorder();
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

            case WORLD_GET_ENTITY_TYPES: {
                return Utils.formatRegEntrySetInfo(
                        Globals.server.getRegistryManager().get(RegistryKeys.ENTITY_TYPE)
                ).getBytes();
            }

            case WORLD_GET_ENTITY: {
                var entity = Globals.world.getEntityById(args.getInt());
                if(entity==null){
                    return null;
                }
                return Utils.formatEntityInfo(entity).getBytes();
            }

            case WORLD_GET_ENTITY_BY_TYPE: {
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
                    max = Globals.world.getWorldBorder().getSize();
                }

                ArrayList<String> ret = new ArrayList<>();
                for (var t : type_arr) {
                    var entities = Globals.world.getEntitiesByType(t.get(), Box.of(Globals.player.getPos(), max, max, max), m -> true);
                    if (entities == null) {
                        continue;
                    }
                    for (Entity entity : entities) {
                        ret.add(Utils.formatEntityInfo(entity));
                    }
                }
                return String.join("|", ret).getBytes();
            }

            case WORLD_SPAWN_ENTITY:{
                String type_name = Utils.readArgString(args);
                double x = args.getDouble();
                double y = args.getDouble();
                double z = args.getDouble();
                String custom_name = Utils.readArgString(args);
                var entity = EntityType.get(type_name).get().create(Globals.world);
                entity.setPos(x, y, z);
                entity.setCustomName(Text.of(custom_name));
                if(Globals.world.spawnEntity(entity)) {
                    return String.valueOf(entity.getId()).getBytes();
                }
                else{
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    return "-1".getBytes();
                }
            }

            case WORLD_REMOVE_ENTITY:{
                var entity = Globals.world.getEntityById(args.getInt());
                if(entity==null){
                    return null;
                }
                entity.remove(Entity.RemovalReason.DISCARDED);
                return "OK".getBytes();
            }

            case WORLD_REMOVE_ENTITY_BY_TYPE:{
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
                    max = Globals.world.getWorldBorder().getSize();
                }

                for (var t : type_arr) {
                    var entities = Globals.world.getEntitiesByType(t.get(), Box.of(Globals.player.getPos(), max, max, max), m -> true);
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

            case WORLD_GET_BLOCK_REGISTRY:{
                return Utils.formatRegEntrySetInfo(
                        Globals.server.getRegistryManager().get(RegistryKeys.BLOCK)
                ).getBytes();
            }

            case WORLD_GET_BLOCK_TYPES: {
                return Utils.formatRegEntrySetInfo(
                        Globals.server.getRegistryManager().get(RegistryKeys.BLOCK_TYPE)
                ).getBytes();
            }

            case WORLD_GET_BLOCK_ENTITY_TYPES:{
                return Utils.formatRegEntrySetInfo(
                        Globals.server.getRegistryManager().get(RegistryKeys.BLOCK_ENTITY_TYPE)
                ).getBytes();
            }

            case WORLD_GET_INFO: {
                return Globals.world.toString().getBytes();
            }

            case WORLD_GET_IS_RAINING: {
                return Globals.world.isRaining()?Constants.Y:Constants.N;
            }

            case WORLD_GET_IS_DAY: {
                return Globals.world.isDay()?Constants.Y:Constants.N;
            }

            case WORLD_GET_TIME: {
                return ByteBuffer.allocate(8)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putLong(Globals.world.getTime()).array();
            }

            case PLAYER_GET_NAME: {
                return Utils.Text2String(Globals.player.getName()).getBytes();
            }

            case PLAYER_GET_POS: {
                return String.format("%s,%s,%s",
                        Globals.player.getX(),
                        Globals.player.getY(),
                        Globals.player.getZ()).getBytes();
            }

            case PLAYER_SET_POS: {
                Globals.player.teleport(args.getDouble(), args.getDouble(), args.getDouble());
                break;
            }

            case PLAYER_GET_SPAWN_POS: {
                var pos = Globals.player.getSpawnPointPosition();
                return String.format("%s,%s,%s,%s,%s",
                        pos.getX(), pos.getY(), pos.getZ(),
                        Globals.player.getSpawnPointDimension().getValue().getPath(),
                        Globals.player.getSpawnAngle()
                ).getBytes();
            }

            case PLAYER_SET_SPAWN_POS: {
                var pos = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                String world_name = Utils.readArgString(args);
                float angle = args.getFloat();
                RegistryKey key = null;
                if(!world_name.isEmpty()){
                    for(var w: Globals.server.getWorlds()){
                        if(world_name.equals(w.getRegistryKey().getValue().getPath())){
                            key = w.getRegistryKey();
                            break;
                        }
                    }
                }
                else{
                    key = Globals.world.getRegistryKey();
                }
                if(key != null) {
                    Globals.player.setSpawnPoint(key, pos, angle, true, true);
                    return Constants.OK;
                }
                else{
                    return Constants.FAILED;
                }
            }

            case PLAYER_GET_LAST_DEAD_POS:{
                var dpos = Globals.player.getLastDeathPos().get();
                if(dpos==null){
                    break;
                }
                var pos = dpos.getPos();
                return String.format("%s,%s,%s,%s",
                        pos.getX(), pos.getY(), pos.getZ(),
                        dpos.getDimension().getValue().getPath()
                ).getBytes();
            }

            case PLAYER_GET_HEALTH:{
                return ByteBuffer.allocate(4)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putFloat(Globals.player.getHealth()).array();
            }

            case PLAYER_SET_HEALTH:{
                var health = args.getFloat();
                Globals.player.setHealth(health);
            }

            case PLAYER_GET_AIR:{
                return ByteBuffer.allocate(4)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(Globals.player.getAir()).array();
            }

            case PLAYER_SET_AIR:{
                var air = args.getInt();
                Globals.player.setAir(air);
            }

            case PLAYER_GET_FOOD_LEVEL:{
                return ByteBuffer.allocate(4)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(Globals.player.getHungerManager().getFoodLevel()).array();
            }

            case PLAYER_SET_FOOD_LEVEL:{
                var level = args.getInt();
                Globals.player.getHungerManager().setFoodLevel(level);
                 Globals.player.getHungerManager().update(Globals.player);
            }

            case PLAYER_GET_MAIN_INVENTORY:{
                return player_getInventoryInfo(Globals.player.getInventory().main, args.getShort());
            }

            case PLAYER_GET_ARMOR_INVENTORY: {
                return player_getInventoryInfo(Globals.player.getInventory().armor, args.getShort());
            }

            case PLAYER_GET_MAIN_HAND: {
                var itemStk = Globals.player.getMainHandStack();
                if(itemStk == null || itemStk.isEmpty()){
                    break;
                }
                return String.format("%s,%d", itemStk.getItem(), itemStk.getCount()).getBytes();
            }


            case PLAYER_GET_OFF_HAND: {
                var itemStk = Globals.player.getOffHandStack();
                if(itemStk == null || itemStk.isEmpty()){
                    break;
                }
                return String.format("%s,%d", itemStk.getItem(), itemStk.getCount()).getBytes();
            }

            case PLAYER_SET_MAIN_HAND: {
                player_setHandItem(Hand.MAIN_HAND, Utils.readArgString(args));
                break;
            }

            case PLAYER_SET_OFF_HAND: {
                player_setHandItem(Hand.OFF_HAND, Utils.readArgString(args));
                break;
            }

            case PLAYER_GET_HOT_BAR_ITEMS: {
                var inv = Globals.player.getInventory();
                ArrayList<String> ret = new ArrayList<>();
                for(var i=0; i<9; i++){
                    var itemStk = inv.getStack(i);
                    if(itemStk != null && !itemStk.isEmpty()){
                        ret.add(String.format("%s,%d", itemStk.getItem(), itemStk.getCount()));
                    }
                    else{
                        ret.add("");
                    }
                }
                return String.join("|", ret).getBytes();
            }

            case PLAYER_SELECT_INVENTORY_SLOT: {
                int index = args.getShort();
                var inv = Globals.player.getInventory();
                if(index==inv.selectedSlot){
                    return Constants.OK;
                }
                var stk = inv.getStack(index);
                if(stk != null) {
                    inv.addPickBlock(stk);
                    return Constants.OK;
                }
                break;
            }

            case PLAYER_DROP_ITEM: {
                boolean all = args.get() != 0;
                if(Globals.player.dropSelectedItem(all)){
                    return Constants.OK;
                }
                break;
            }

            case PLAYER_DESTROY_ITEM: {
                int index = args.getShort();
                var inv = Globals.player.getInventory();
                if(index<0){
                    index = inv.selectedSlot;
                }
                var it = inv.getStack(index);
                if(it != null) {
                    inv.removeStack(index);
                    Globals.player.playerScreenHandler.syncState();
                    Globals.player.playerScreenHandler.updateToClient();
                    return Constants.OK;
                }
                break;
            }

            case PLAYER_SET_INVENTORY_SLOT: {
                int index = args.getShort();
                String item_name = Utils.readArgString(args);
                int item_count = args.getShort();
                var inv = Globals.player.getInventory();
                if(index<0){
                    index = inv.selectedSlot;
                }
                var item =Registries.ITEM.get(Identifier.tryParse(item_name));
                if(item != null) {
                    inv.setStack(index, new ItemStack(item, item_count));
                    return Constants.OK;
                }
                break;
            }

            case SEND_MESSAGE: {
                if(Globals.player!=null) {
                    Globals.player.sendMessage(Text.of(Utils.readArgString(args)));
                }
                else{
                    Globals.server.sendMessage(Text.of(Utils.readArgString(args)));
                }
                break;
            }

            case EXECUTE_COMMAND:{
                if(Globals.player!=null) {
                    var src = Globals.player.getCommandSource();
                    Globals.server.getCommandManager().executeWithPrefix(src, Utils.readArgString(args));
                }
                else{
                    Globals.server.getCommandManager().executeWithPrefix(Globals.server.getCommandSource(), Utils.readArgString(args));
                }
                break;
            }

            default:
                return Constants.ERR_UNKNOWN_COMMAND;
        }

        return null;
    }


    private @NotNull byte[] player_getInventoryInfo(DefaultedList<ItemStack> inv, int index) {

        if(index>=0 && index<inv.size()){
            var itemStk = inv.get(index);
            return String.format("%s,%d", itemStk.getItem(), itemStk.getCount()).getBytes();
        }

        ArrayList<String> ret = new ArrayList<>();
        for(var itemStk : inv) {
            if(!itemStk.isEmpty()){
                ret.add(String.format("%s,%d", itemStk.getItem(), itemStk.getCount()));
            }
            else{
                ret.add("");
            }
        }
        return String.join("|", ret).getBytes();
    }

    private void player_setHandItem(Hand hand, String item_name){
        if(item_name==null || item_name.isEmpty()){
            return;
        }
        var inv = Globals.player.getInventory();
        var selStack = inv.getStack(inv.selectedSlot);
        if(item_name.equals(selStack.getItem().toString())) {
            return;
        }
        for(var slot: Globals.player.playerScreenHandler.slots) {
            if (slot.hasStack() && slot.isEnabled() && item_name.equals(slot.getStack().getItem().toString())) {
                ItemStack itemStack = slot.takeStack(1);
                if(itemStack!=null && !itemStack.isEmpty()) {
                    inv.setStack(slot.getIndex(), selStack);
                    Globals.player.setStackInHand(hand, itemStack);
                    return;
                }
            }
        }
     }

    private ServerPlayerEntity tryGetPlayer(String name, int player_id){
        var mgr = Globals.server.getPlayerManager();
        if(name!=null && !name.isEmpty()){
            return mgr.getPlayer(name);
        }
        else if(player_id > 0){
            for (var p : mgr.getPlayerList()) {
                if (p.getId() == player_id) {
                    return p;
                }
            }
        }
        else{
            if(mgr.getCurrentPlayerCount()>0) {
                return mgr.getPlayerList().get(0);
            }
        }

        return null;
    }
}
