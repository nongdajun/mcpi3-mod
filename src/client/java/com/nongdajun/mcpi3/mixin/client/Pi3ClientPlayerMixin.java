package com.nongdajun.mcpi3.mixin.client;

import com.nongdajun.mcpi3.api.HandlerHub;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.nongdajun.mcpi3.api.client.ClientHandler;

@Mixin(ClientPlayerEntity.class)
public abstract class Pi3ClientPlayerMixin {

    /*
    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSneaking()Z"))
    public boolean sendMovementPackets_isSneaking(ClientPlayerEntity instance) {
        return instance.isSneaking();
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSneaking()Z"))
    public boolean tickMovement_isSneaking(ClientPlayerEntity instance) {
        return instance.isSneaking();
    }*/

    @Inject(method = "isSneaking()Z", at = @At("RETURN"), cancellable = true)
    public void _isSneaking(CallbackInfoReturnable<Boolean> cir) {
        if(HandlerHub.clientHandler != null){
            var b = ((ClientHandler)HandlerHub.clientHandler).player_is_sneaking;
            if(b){
                cir.setReturnValue(true);
            }
        }
    }

}
