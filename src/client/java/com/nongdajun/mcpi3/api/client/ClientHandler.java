package com.nongdajun.mcpi3.api.client;

import com.nongdajun.mcpi3.api.Commands;
import com.nongdajun.mcpi3.api.Constants;
import com.nongdajun.mcpi3.api.Utils;
import com.nongdajun.mcpi3.api.handlers.ICommandHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class ClientHandler implements ICommandHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger("pi3_client_handler");

    @Override
    public byte[] execute(int cmd_code, ByteBuffer args) {

        var client = ClientGlobals.client;
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
                client.player.updateVelocity(0.3f, new Vec3d(0, 0, d));
                client.player.move(MovementType.SELF, client.player.getVelocity());
                break;
            }

            case PLAYER_MOVE_BACKWARD:{
                var d = args.getFloat();
                if(d<=0.0f) d = 1.0f;
                client.player.updateVelocity(0.3f, new Vec3d(0, 0, -d));
                client.player.move(MovementType.SELF, client.player.getVelocity());
                break;
            }

            case PLAYER_MOVE_LEFT:{
                var d = args.getFloat();
                if(d<=0.0f) d = 1.0f;
                client.player.updateVelocity(0.3f, new Vec3d(d, 0, 0));
                client.player.move(MovementType.SELF, client.player.getVelocity());
                break;
            }

            case PLAYER_MOVE_RIGHT:{
                var d = args.getFloat();
                if(d<=0.0f) d = 1.0f;
                client.player.updateVelocity(0.3f, new Vec3d(-d, 0, 0));
                client.player.move(MovementType.SELF, client.player.getVelocity());
                break;
            }

            case PLAYER_MOVE_TO:{
                var pos = new Vec3d(args.getFloat(), args.getFloat(), args.getFloat());
                client.player.updateVelocity(0.3f, pos);
                client.player.move(MovementType.PLAYER, client.player.getVelocity());
                break;
            }

            case PLAYER_JUMP: {
                client.player.jump();
                break;
            }

            case PLAYER_ATTACK: {
                var target = client.crosshairTarget;
                if(target==null){
                    return Constants.FAILED;
                }
                boolean ret = false;
                switch (target.getType()){
                    case BLOCK:
						BlockHitResult blockHitResult = (BlockHitResult)client.crosshairTarget;
						BlockPos blockPos = blockHitResult.getBlockPos();
						if (!client.world.getBlockState(blockPos).isAir()) {
							client.interactionManager.attackBlock(blockPos, blockHitResult.getSide());
							if (client.world.getBlockState(blockPos).isAir()) {
								ret = true;
							}
							break;
						}
                    case ENTITY:
                        client.interactionManager.attackEntity(client.player, client.targetedEntity);
                        ret = true;
                        break;
 					case MISS:
						if (client.interactionManager.hasLimitedAttackSpeed()) {
							client.attackCooldown = 10;
						}

                        client.player.resetLastAttackedTicks();
               }

                client.player.swingHand(Hand.MAIN_HAND);

                if(ret){
                    return Constants.OK;
                }
                else{
                    return Constants.FAILED;
                }
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
                if (!client.interactionManager.isBreakingBlock()) {
                    if (!client.player.isRiding()) {
                        if (client.crosshairTarget == null) {
                            LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
                        }

                        for (Hand hand : Hand.values()) {
                            ItemStack itemStack = client.player.getStackInHand(hand);
                            if (!itemStack.isItemEnabled(client.world.getEnabledFeatures())) {
                                break;
                            }

                            if (client.crosshairTarget != null) {
                                switch (client.crosshairTarget.getType()) {
                                    case ENTITY:
                                        EntityHitResult entityHitResult = (EntityHitResult)client.crosshairTarget;
                                        Entity entity = entityHitResult.getEntity();
                                        if (!client.world.getWorldBorder().contains(entity.getBlockPos())) {
                                            break;
                                        }

                                        ActionResult actionResult = client.interactionManager.interactEntityAtLocation(client.player, entity, entityHitResult, hand);
                                        if (!actionResult.isAccepted()) {
                                            actionResult = client.interactionManager.interactEntity(client.player, entity, hand);
                                        }

                                        if (actionResult.isAccepted()) {
                                            if (actionResult.shouldSwingHand()) {
                                                client.player.swingHand(hand);
                                            }

                                            break;
                                        }
                                        break;
                                    case BLOCK:
                                        BlockHitResult blockHitResult = (BlockHitResult)client.crosshairTarget;
                                        int i = itemStack.getCount();
                                        ActionResult actionResult2 = client.interactionManager.interactBlock(client.player, hand, blockHitResult);
                                        if (actionResult2.isAccepted()) {
                                            if (actionResult2.shouldSwingHand()) {
                                                client.player.swingHand(hand);
                                                if (!itemStack.isEmpty() && (itemStack.getCount() != i || client.interactionManager.hasCreativeInventory())) {
                                                    client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                                                }
                                            }

                                            break;
                                        }

                                        if (actionResult2 == ActionResult.FAIL) {
                                            break;
                                        }
                                }
                            }

                            if (!itemStack.isEmpty()) {
                                ActionResult actionResult3 = client.interactionManager.interactItem(client.player, hand);
                                if (actionResult3.isAccepted()) {
                                    if (actionResult3.shouldSwingHand()) {
                                        client.player.swingHand(hand);
                                    }

                                    client.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                                    break;
                                }
                            }
                        }
                    }
                }
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

            case PLAYER_SET_MAIN_HAND: {
                player_setHandItem(Hand.MAIN_HAND, Utils.readArgString(args));
                break;
            }

            case PLAYER_SET_OFF_HAND: {
                player_setHandItem(Hand.OFF_HAND, Utils.readArgString(args));
                break;
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

            case PLAYER_PICK_ITEM: {
                int index = args.getShort();
                var inv = client.player.getInventory();
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
                if(client.player.dropSelectedItem(all)){
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

    private void player_setHandItem(Hand hand, String item_name){
        if(item_name==null || item_name.isEmpty()){
            return;
        }
        var inv = ClientGlobals.client.player.getInventory();
        var selStack = inv.getStack(inv.selectedSlot);
        if(item_name.equals(selStack.getItem().toString())) {
            return;
        }
        for(var slot: ClientGlobals.client.player.playerScreenHandler.slots) {
            if (slot.hasStack() && slot.isEnabled() && item_name.equals(slot.getStack().getItem().toString())) {
                ItemStack itemStack = slot.takeStack(1);
                if(itemStack!=null && !itemStack.isEmpty()) {
                    inv.setStack(slot.getIndex(), selStack);
                    ClientGlobals.client.player.setStackInHand(hand, itemStack);
                    return;
                }
            }
        }
     }

}
