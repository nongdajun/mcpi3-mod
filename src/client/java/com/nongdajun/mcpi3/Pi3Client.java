package com.nongdajun.mcpi3;

import com.nongdajun.mcpi3.api.Globals;
import com.nongdajun.mcpi3.conn.Connection;
import net.fabricmc.api.ClientModInitializer;
import com.nongdajun.mcpi3.api.client.*;

public class Pi3Client implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		Connection.init();

	}
}