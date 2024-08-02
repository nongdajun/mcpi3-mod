package com.nongdajun.mcpi3.mixin.client;

import com.nongdajun.mcpi3.api.HandlerHub;
import com.nongdajun.mcpi3.api.client.ClientGlobals;
import com.nongdajun.mcpi3.api.client.ClientHandler;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class Pi3ClientMixin {
	@Inject(at = @At("HEAD"), method = "run")
	private void init_run(CallbackInfo info) {
		// This code is injected into the start of MinecraftClient.run()V
		ClientGlobals.client = MinecraftClient.getInstance();
		if(HandlerHub.clientHandler == null){
			ClientHandler.LOGGER.info("ClientHandler is creating ...");
			HandlerHub.clientHandler = new ClientHandler();
		}
	}

	@Inject(at = @At("HEAD"), method = "stop")
	private void init_stop(CallbackInfo info) {
		// This code is injected into the start of MinecraftClient.stop()V
		HandlerHub.clientHandler = null;
		ClientHandler.LOGGER.info("ClientHandler removed!");
	}

}