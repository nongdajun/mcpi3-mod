package com.nongdajun.mcpi3.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Config {

    public static final Logger LOGGER = LoggerFactory.getLogger("pi3");

    public static int PORT = 5647;

    public static boolean ENABLE_REMOTE = false;

    public static final int VERSION = 1;

    public static String getHost(){
        return ENABLE_REMOTE ? "0.0.0.0" : "127.0.0.1";
    }

    static {
        if(!Load()){
            Save();
        }
    }

    public static boolean Load(){
        Path currentDir = Paths.get("").toAbsolutePath();
		Path configDir = currentDir.resolve("config");
        if(!configDir.toFile().isDirectory()){
            LOGGER.warn("Config directory not found!");
            return false;
        }
		Path configFile = configDir.resolve("pi3_config.json");
        if(!configFile.toFile().isFile()){
            return false;
        }

        try {
                FileInputStream is = new FileInputStream(configFile.toFile());
                var buff = is.readAllBytes();
                is.close();

                var json_ele = JsonParser.parseString(new String(buff));

                if(!json_ele.isJsonObject()){
                    LOGGER.error("Config file is not a json object!");
                    return false;
                }

                JsonObject json = json_ele.getAsJsonObject();
                PORT = json.get("port").getAsInt();
                ENABLE_REMOTE = json.get("enable_remote").getAsBoolean();
            } catch (FileNotFoundException e) {
                LOGGER.error("Failed to open config file!, error: {}", e);
                return false;
            } catch (IOException e) {
                LOGGER.error("Failed to read config file!, error: {}", e);
                return false;
            }

        return true;
    }
    public static void Save(){
        Path currentDir = Paths.get("").toAbsolutePath();
		Path configDir = currentDir.resolve("config");
        if(!configDir.toFile().isDirectory()) {
            LOGGER.warn("Config directory not found! creating...");
            try {
                configDir.toFile().mkdir();
            } catch (Exception ex) {
                LOGGER.error("Failed to create config directory!");
                return;
            }
        }

        Path configFile = configDir.resolve("pi3_config.json");
        try {
            FileOutputStream os = new FileOutputStream(configFile.toFile(), false);
            JsonObject json = new JsonObject();
            json.addProperty("port", PORT);
            json.addProperty("enable_remote", ENABLE_REMOTE);
            os.write(json.toString().getBytes());
            os.close();
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to open config file!, error: {}", e);
        } catch (IOException e) {
            LOGGER.error("Failed to write config file!, error: {}", e);
        }


    }
}
