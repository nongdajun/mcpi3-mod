package com.nongdajun.mcpi3.api;

public final class Commands {

    public enum Common {
        PING,
        GET_API_VERSION,
        IS_SERVER_READY,
        IS_CLIENT_READY,
        SET_ECHO_MODE,
        DEBUG
    }

    public enum Server {
        GET_PLAYERS,
        ATTACH_PLAYER,
        CURRENT_PLAYER,
        GET_WORLDS,
        ATTACH_WORLD,
        CURRENT_WORLD,
        KILL_PLAYER,
        GET_GAME_MODE,
        GET_GAME_VERSION,

        __WORLD_COMMANDS_START__,
        WORLD_GET_NAME,
        WORLD_GET_BLOCK,
        WORLD_SET_BLOCK,
        WORLD_GET_BLOCK_WITH_DATA,
        WORLD_GET_BLOCKS,
        WORLD_SET_BLOCKS,
        WORLD_GET_PLAYERS,
        WORLD_GET_BORDER,
        WORLD_GET_ENTITY_TYPES,
        WORLD_GET_ENTITY,
        WORLD_GET_ENTITY_BY_TYPE,
        WORLD_SPAWN_ENTITY,
        WORLD_REMOVE_ENTITY,
        WORLD_REMOVE_ENTITY_BY_TYPE,
        WORLD_GET_BLOCK_REGISTRY,
        WORLD_GET_BLOCK_TYPES,
        WORLD_GET_BLOCK_ENTITY_TYPES,
        WORLD_GET_INFO,
        WORLD_GET_IS_RAINING,
        WORLD_GET_IS_DAY,
        WORLD_GET_TIME,
        __WORLD_COMMANDS_END__,

        __PLAYER_COMMANDS_START__,
        PLAYER_GET_NAME,
        PLAYER_GET_POS,
        PLAYER_SET_POS,
        PLAYER_GET_SPAWN_POS,
        PLAYER_SET_SPAWN_POS,
        PLAYER_GET_LAST_DEAD_POS,
        PLAYER_GET_HEALTH,
        PLAYER_SET_HEALTH,
        PLAYER_GET_FOOD_LEVEL,
        PLAYER_SET_FOOD_LEVEL,
        PLAYER_GET_AIR,
        PLAYER_SET_AIR,
        PLAYER_GET_MAIN_INVENTORY,
        PLAYER_GET_ARMOR_INVENTORY,
        PLAYER_GET_MAIN_HAND,
        PLAYER_GET_OFF_HAND,
        PLAYER_SET_MAIN_HAND,
        PLAYER_SET_OFF_HAND,
        PLAYER_GET_HOT_BAR_ITEMS,
        PLAYER_SELECT_INVENTORY_SLOT,
        PLAYER_DROP_ITEM,
        PLAYER_DESTROY_ITEM,
        PLAYER_SET_INVENTORY_SLOT,
        __PLAYER_COMMANDS_END__,

        SEND_MESSAGE,
        EXECUTE_COMMAND,
    }

    public enum Client {
        PLAYER_MOVE_FORWARD,
        PLAYER_MOVE_BACKWARD,
        PLAYER_MOVE_LEFT,
        PLAYER_MOVE_RIGHT,
        PLAYER_MOVE_TO,
        PLAYER_JUMP,
        PLAYER_ATTACK,
        PLAYER_ATTACK_ENTITY,
        PLAYER_ATTACK_BLOCK,
        PLAYER_LOOKING_AT,
        PLAYER_SWING_HAND,
        PLAYER_USE_ITEM,
        PLAYER_PICK_ITEM,
        PLAYER_SWAP_HANDS,
        PLAYER_SET_SNEAK,

        PLAYER_GET_NAME,
        PLAYER_GET_POS,
        PLAYER_SET_POS,
        PLAYER_GET_SPAWN_POS,
        PLAYER_SET_SPAWN_POS,
        PLAYER_GET_LAST_DEAD_POS,
        PLAYER_GET_HEALTH,
        PLAYER_GET_AIR,
        PLAYER_GET_FOOD_LEVEL,
        PLAYER_GET_MAIN_INVENTORY,
        PLAYER_GET_ARMOR_INVENTORY,
        PLAYER_GET_MAIN_HAND,
        PLAYER_GET_OFF_HAND,
        PLAYER_GET_HOT_BAR_ITEMS,
        PLAYER_SELECT_INVENTORY_SLOT,
        PLAYER_DROP_ITEM,
        PLAYER_DESTROY_ITEM,

        PLAYER_CHANGE_LOOK_DIRECTION,
        PLAYER_CHANGE_PERSPECTIVE,

        WORLD_GET_SPAWN_POS,
        WORLD_SET_SPAWN_POS,
        GET_WORLDS,
        WORLD_GET_NAME,
        WORLD_GET_BLOCK,
        WORLD_GET_BLOCK_WITH_DATA,
        WORLD_GET_BLOCKS,
        WORLD_GET_PLAYERS,
        WORLD_GET_BORDER,
        WORLD_GET_ENTITY_TYPES,
        WORLD_GET_ENTITY,
        WORLD_GET_ENTITY_BY_TYPE,
        WORLD_GET_BLOCK_REGISTRY,
        WORLD_GET_BLOCK_TYPES,
        WORLD_GET_BLOCK_ENTITY_TYPES,
        WORLD_GET_INFO,
        WORLD_GET_IS_RAINING,
        WORLD_GET_IS_DAY,
        WORLD_GET_TIME,

        GET_GAME_MODE,
        GET_GAME_VERSION,
        GET_CLIENT_VERSION,
    }

    private static Common[] _CommonSet = Common.values();
    private static Server[] _ServerSet = Server.values();
    private static Client[] _ClientSet = Client.values();

    public static final int CommonCommandMask = 0x1000;
    public static final int ServerCommandMask = 0x2000;
    public static final int ClientCommandMask = 0x3000;

    public static Common codeToCommon(int code) {
        return _CommonSet[code&0xfff];
    }

    public static Server codeToServer(int code) {
        return _ServerSet[code&0xfff];
    }

    public static Client codeToClient(int code) {
        return _ClientSet[code&0xfff];
    }
}
