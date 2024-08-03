package com.nongdajun.mcpi3.api.client;

import com.nongdajun.mcpi3.api.Commands;
import com.nongdajun.mcpi3.api.Constants;
import com.nongdajun.mcpi3.api.Utils;
import com.nongdajun.mcpi3.api.handlers.ICommandHandler;
import com.nongdajun.mcpi3.mixin.client.MinecraftClientAccessor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Optional;

public class ClientHandler implements ICommandHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger("pi3_client_handler");

    public boolean player_is_sneaking = false;

    @Override
    public byte[] execute(int cmd_code, ByteBuffer args) {

        var client = ClientGlobals.client;
        var client_accessor = (MinecraftClientAccessor) client;

        if(client==null){
            return Constants.ERR_CLIENT_NOT_READY;
        }

        Commands.Client op;
        try{
            op = Commands.codeToClient(cmd_code);
        }
        catch (Exception ex){
            return Constants.ERR_UNKNOWN_COMMAND;
        }

        switch (op) {

            case PLAYER_MOVE_FORWARD:{
                var d = args.getFloat();
                if(d<=0.0f) d = 1.0f;
                var sp = client.player.getMovementSpeed();
                client.player.updateVelocity(sp*d, new Vec3d(0, 0, 1));
                client.player.move(MovementType.SELF, client.player.getVelocity());
                break;
            }

            case PLAYER_MOVE_BACKWARD:{
                var d = args.getFloat();
                if(d<=0.0f) d = 1.0f;
                var sp = client.player.getMovementSpeed();
                client.player.updateVelocity(sp*d, new Vec3d(0, 0, -1));
                client.player.move(MovementType.SELF, client.player.getVelocity());
                break;
            }

            case PLAYER_MOVE_LEFT:{
                var d = args.getFloat();
                if(d<=0.0f) d = 1.0f;
                var sp = client.player.getMovementSpeed();
                client.player.updateVelocity(sp*d, new Vec3d(1, 0, 0));
                client.player.move(MovementType.SELF, client.player.getVelocity());
                break;
            }

            case PLAYER_MOVE_RIGHT:{
                var d = args.getFloat();
                if(d<=0.0f) d = 1.0f;
                var sp = client.player.getMovementSpeed();
                client.player.updateVelocity(sp*d, new Vec3d(-1, 0, 0));
                client.player.move(MovementType.SELF, client.player.getVelocity());
                break;
            }

            case PLAYER_MOVE_TO:{
                var pos = new Vec3d(args.getFloat(), args.getFloat(), args.getFloat());
                var d = args.getFloat();
                if(d<=0.0f) d = 1.0f;
                var sp = client.player.getMovementSpeed();
                client.player.updateVelocity(sp*d, pos);
                client.player.move(MovementType.PLAYER, client.player.getVelocity());
                break;
            }

            case PLAYER_JUMP: {
                client.player.jump();
                break;
            }

            case PLAYER_ATTACK: {
                client.attackCooldown = 0;
                client_accessor._doAttack();
                break;
            }

            case PLAYER_ATTACK_ENTITY: {
                var target = client.world.getEntityById(args.getInt());
                if(target!=null){
                    if(client.player.tryAttack(target)){
                        return Constants.OK;
                    }
                }
                return Constants.FAILED;
            }

            case PLAYER_ATTACK_BLOCK: {
                var target = client.world.getBlockEntity(new BlockPos(args.getInt(), args.getInt(), args.getInt()));
                if(target!=null){
                    if(client.world.breakBlock(target.getPos(), false)){
                        return Constants.OK;
                    }
                }
                return Constants.FAILED;
            }

            case PLAYER_LOOKING_AT:{
                var target = client.crosshairTarget;
                if(target==null){
                    return null;
                }
                var pos = target.getPos();
                switch (target.getType()){
                    case BLOCK:
                        return String.format("%s,%s,%s,block", pos.x, pos.y, pos.z).getBytes();
                    case ENTITY:
                        return String.format("%s,%s,%s,entity", pos.x, pos.y, pos.z).getBytes();
                }
                return String.format("%s,%s,%s,miss", pos.x, pos.y, pos.z).getBytes();
            }

            case PLAYER_SWING_HAND:{
                var off_hand = args.get() !=0 ;
                client.player.swingHand(off_hand?Hand.OFF_HAND:Hand.MAIN_HAND);
            }

            case PLAYER_USE_ITEM:{
                client_accessor._doItemUse();
                break;
            }

            case PLAYER_PICK_ITEM:{
                client_accessor._doItemPick();
                break;
            }

            case PLAYER_SWAP_HANDS:{
                if (!client.player.isSpectator()) {
                    client.getNetworkHandler()
                        .sendPacket(
                            new PlayerActionC2SPacket(net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN)
                        );
			    }
                break;
            }

            case PLAYER_SET_SNEAK:{
                this.player_is_sneaking = (args.get()!=0);
                break;
            }

            case PLAYER_GET_NAME: {
                return Utils.Text2String(client.player.getName()).getBytes();
            }

            case PLAYER_GET_POS: {
                return String.format("%s,%s,%s",
                        client.player.getX(),
                        client.player.getY(),
                        client.player.getZ()).getBytes();
            }

            case PLAYER_SET_POS: {
                client.player.teleport(args.getDouble(), args.getDouble(), args.getDouble());
                break;
            }

            case WORLD_GET_SPAWN_POS:
            case PLAYER_GET_SPAWN_POS: {
                var pos = client.world.getSpawnPos();
                return String.format("%s,%s,%s,%s",
                        pos.getX(), pos.getY(), pos.getZ(),
                        client.world.getSpawnAngle()
                ).getBytes();
            }

            case WORLD_SET_SPAWN_POS:
            case PLAYER_SET_SPAWN_POS: {
                var pos = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                float angle = args.getFloat();
                client.world.setSpawnPos(pos, angle);
                break;
            }

            case PLAYER_GET_LAST_DEAD_POS:{
                var dpos = client.player.getLastDeathPos().get();
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
                        .putFloat(client.player.getHealth()).array();
            }

            case PLAYER_GET_AIR:{
                return ByteBuffer.allocate(4)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(client.player.getAir()).array();
            }

            case PLAYER_GET_FOOD_LEVEL:{
                return ByteBuffer.allocate(4)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(client.player.getHungerManager().getFoodLevel()).array();
            }

            case PLAYER_GET_MAIN_INVENTORY:{
                return player_getInventoryInfo(client.player.getInventory().main, args.getShort());
            }

            case PLAYER_GET_ARMOR_INVENTORY: {
                return player_getInventoryInfo(client.player.getInventory().armor, args.getShort());
            }

            case PLAYER_GET_MAIN_HAND: {
                var itemStk = client.player.getMainHandStack();
                if(itemStk == null || itemStk.isEmpty()){
                    break;
                }
                return String.format("%s,%d", itemStk.getItem(), itemStk.getCount()).getBytes();
            }


            case PLAYER_GET_OFF_HAND: {
                var itemStk = client.player.getOffHandStack();
                if(itemStk == null || itemStk.isEmpty()){
                    break;
                }
                return String.format("%s,%d", itemStk.getItem(), itemStk.getCount()).getBytes();
            }

            case PLAYER_GET_HOT_BAR_ITEMS: {
                var inv = client.player.getInventory();
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
                var inv = client.player.getInventory();
                if(index == inv.selectedSlot){
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
                byte a = args.get();
                boolean all;
                if(a==0xF){
                    all = Screen.hasControlDown();
                }
                else{
                    all = a!=0;
                }
                if(client.player.isSpectator()==false && client.player.dropSelectedItem(all)){
                    client.player.swingHand(Hand.MAIN_HAND);
                    return Constants.OK;
                }
                break;
            }

            case PLAYER_DESTROY_ITEM: {
                int index = args.getShort();
                var inv = client.player.getInventory();
                if(index<0){
                    index = inv.selectedSlot;
                }
                var it = inv.getStack(index);
                if(it != null) {
                    inv.removeStack(index);
                    client.player.playerScreenHandler.syncState();
                    return Constants.OK;
                }
                break;
            }

            case PLAYER_CHANGE_LOOK_DIRECTION:{
                var dx = args.getFloat();
                var dy = args.getFloat();
                client.player.changeLookDirection(dx/360.0*2400.0,dy/360.0*2400.0);
                break;
            }

            case PLAYER_CHANGE_PERSPECTIVE:{
                var isFirst = args.get()!=0;
                Perspective perspective = client.options.getPerspective();
                if(isFirst == perspective.isFirstPerson()){
                    break;
                }
    			client.options.setPerspective(client.options.getPerspective().next());
                if (perspective.isFirstPerson() != client.options.getPerspective().isFirstPerson()) {
                    client.gameRenderer.onCameraEntitySet(client.options.getPerspective().isFirstPerson() ? client.getCameraEntity() : null);
                }
    			client.worldRenderer.scheduleTerrainUpdate();
                break;
            }

            case GET_WORLDS:{
                ArrayList<String> ret = new ArrayList<>();
                for (var w: client.getServer().getWorldRegistryKeys()) {
                    ret.add(w.getValue().getPath());
                }
                return String.join("|", ret).getBytes();
            }

            case WORLD_GET_NAME:{
                if(client.world!=null) {
                    return client.world.getRegistryKey().getValue().getPath().getBytes();
                }
            }

            case WORLD_GET_BLOCK: {
                var block_state = client.world.getBlockState(new BlockPos(args.getInt(), args.getInt(), args.getInt()));
                if(block_state==null){
                    return null;
                }
                return Utils.formatBlockInfo(block_state).getBytes();
            }

            case WORLD_GET_BLOCK_WITH_DATA: {
                var block = client.world.getBlockState(new BlockPos(args.getInt(), args.getInt(), args.getInt()));
                if(block==null){
                    return null;
                }
                return block.toString().getBytes();
            }

            case WORLD_GET_BLOCKS: {
                var pos0 = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                var pos1 = new BlockPos(args.getInt(), args.getInt(), args.getInt());
                var blocks = client.world.getStatesInBoxIfLoaded(Box.enclosing(pos0, pos1));
                if(blocks==null){
                    return null;
                }
                ArrayList<String> ret = new ArrayList<>();
                for(var bs: blocks.toList()) {
                    var block = bs.getBlock();
                    ret.add(bs.getRegistryEntry().getKey().get().getValue().getPath());
                }
                return String.join("|", ret).getBytes();
            }

            case WORLD_GET_PLAYERS: {
                ArrayList<String> ret = new ArrayList<>();
                for (var player : client.world.getPlayers()) {
                    ret.add(Utils.formatPlayerInfo(player));
                }
                return String.join("|", ret).getBytes();
            }

            case WORLD_GET_BORDER: {
                var bdr = client.world.getWorldBorder();
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
                        client.getServer().getRegistryManager().get(RegistryKeys.ENTITY_TYPE)
                ).getBytes();
            }

            case WORLD_GET_ENTITY: {
                var entity = client.world.getEntityById(args.getInt());
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
                    max = client.world.getWorldBorder().getSize();
                }

                ArrayList<String> ret = new ArrayList<>();
                for (var t : type_arr) {
                    var entities = client.world.getEntitiesByType(t.get(), Box.of(client.player.getPos(), max, max, max), m -> true);
                    if (entities == null) {
                        continue;
                    }
                    for (Entity entity : entities) {
                        ret.add(Utils.formatEntityInfo(entity));
                    }
                }
                return String.join("|", ret).getBytes();
            }

            case WORLD_GET_BLOCK_REGISTRY:{
                return Utils.formatRegEntrySetInfo(
                        client.getServer().getRegistryManager().get(RegistryKeys.BLOCK)
                ).getBytes();
            }

            case WORLD_GET_BLOCK_TYPES: {
                return Utils.formatRegEntrySetInfo(
                        client.getServer().getRegistryManager().get(RegistryKeys.BLOCK_TYPE)
                ).getBytes();
            }

            case WORLD_GET_BLOCK_ENTITY_TYPES:{
                return Utils.formatRegEntrySetInfo(
                        client.getServer().getRegistryManager().get(RegistryKeys.BLOCK_ENTITY_TYPE)
                ).getBytes();
            }

            case WORLD_GET_INFO: {
                return client.world.toString().getBytes();
            }

            case WORLD_GET_IS_RAINING: {
                return client.world.isRaining()?Constants.Y:Constants.N;
            }

            case WORLD_GET_IS_DAY: {
                return client.world.isDay()?Constants.Y:Constants.N;
            }

            case WORLD_GET_TIME: {
                return ByteBuffer.allocate(8)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putLong(client.world.getTime()).array();
            }

            case GET_GAME_MODE:{
                return client.interactionManager.getCurrentGameMode().getName().getBytes();
            }

            case GET_GAME_VERSION:{
                return client.getServer().getVersion().toString().getBytes();
            }

            case GET_CLIENT_VERSION:{
                return client.getGameVersion().getBytes();
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
}
