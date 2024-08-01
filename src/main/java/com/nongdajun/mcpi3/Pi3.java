package com.nongdajun.mcpi3;

import com.nongdajun.mcpi3.api.Globals;
import com.nongdajun.mcpi3.api.HandlerHub;
import com.nongdajun.mcpi3.api.handlers.CommonHandler;
import com.nongdajun.mcpi3.conn.Connection;
import com.nongdajun.mcpi3.debug.TestHelper;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.*;


public class Pi3 implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("pi3");

	private boolean wait_for_player_flag = true;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("=== Hello π3 world! ===");

		Connection.init();
		HandlerHub.commonHandler = new CommonHandler();

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			Globals.server = server;
			Globals.world = null;
			Globals.player = null;
			wait_for_player_flag = true;

			LOGGER.info("Pi3 CONNECT // : {}", handler.getSide().name());

			if(HandlerHub.serverHandler==null){
				LOGGER.info("ServerHandler is creating ...");
				HandlerHub.serverHandler = new com.nongdajun.mcpi3.api.handlers.ServerHandler();
			}

		});

		ServerConfigurationConnectionEvents.DISCONNECT.register((handler, server) -> {
			Globals.server = null;
			Globals.world = null;
			Globals.player = null;
			wait_for_player_flag = false;
			HandlerHub.serverHandler = null;
			LOGGER.info("ServerHandler removed!");
			LOGGER.info("Pi3 DISCONNECT // : {}", handler.getSide());
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if(wait_for_player_flag){
				wait_for_player_flag = false;
				Globals.player = handler.player;
				Globals.world = handler.player.getWorld();
				LOGGER.info("Pi3 FIRST PLAYER JOIN // : {} @ {}", handler.player.getName().getString(), Globals.world.getRegistryKey().getValue().getPath());
			}
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("pi3")
			.executes(context -> {
			  try{
				  TestHelper.run();
				  context.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("INVOKE /pi3 SUCCESS!"), false);
			  }
			  catch (Exception ex){
				  context.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("INVOKE /pi3 ERROR: "+ ex.getMessage()), false);
			  }
			  return 1;
		})));
	}
}