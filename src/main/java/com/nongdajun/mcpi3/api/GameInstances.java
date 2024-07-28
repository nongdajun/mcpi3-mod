package com.nongdajun.mcpi3.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.ArrayList;

public class GameInstances {

	public static MinecraftServer server;
	public static ServerPlayerEntity player;
	public static World world;
	public static ArrayList<ServerPlayerEntity> players = new ArrayList<ServerPlayerEntity>();

}
