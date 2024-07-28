package com.nongdajun.mcpi3;

import com.nongdajun.mcpi3.api.GameInstances;
import com.nongdajun.mcpi3.conn.Connection;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pi3 implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("pi3");

	public Connection conn;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("=== Hello π3 world! ===");

		conn = new Connection("localhost", 5647);

		conn.init();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			GameInstances.server = server;

			if(GameInstances.player==null) {
				GameInstances.player = handler.player;
	            LOGGER.info(String.format("CONNECT Pi3 // auto attach player: %s", handler.player.getName().getString()));
			}
			if(!GameInstances.players.contains(handler.player)){
				GameInstances.players.add(handler.player);
			}
			GameInstances.world = handler.player.getWorld();
        });

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			if(handler.player == GameInstances.player){
				GameInstances.player = null;
	            LOGGER.info(String.format("DISCONNECT Pi3 // detach player: %s", handler.player.getName().getString()));
			}
			GameInstances.players.remove(handler.player);
        });

	}
}