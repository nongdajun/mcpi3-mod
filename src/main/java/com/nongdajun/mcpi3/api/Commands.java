package com.nongdajun.mcpi3.api;

public final class Commands {

    public final class Option {
        public static final int PING = 0x01;
        public static final int HELLO = 0x02;
        public static final int READY = 0x03;
        public static final int ECHO = 0x04;
        public static final int ATTACH_PLAYER = 0x05;
        public static final int CURRENT_PLAYER = 0x06;
        public static final int DEBUG = 0xdb;
    }

    public final class World {
        public static final int GET_BLOCK = 0x101;
        public static final int SET_BLOCK = 0x102;
        public static final int GET_BLOCK_WITH_DATA = 0x103;
        public static final int GET_BLOCKS = 0x104;
        public static final int SET_BLOCKS = 0x105;
        public static final int GET_PLAYERS = 0x106;
        public static final int GET_BORDER = 0x107;
        public static final int GET_ENTITY_TYPES = 0x108;
        public static final int GET_ENTITY = 0x109;
        public static final int GET_ENTITY_BY_TYPE = 0x10a;
        public static final int SPAWN_ENTITY = 0x10b;
        public static final int REMOVE_ENTITY = 0x10c;
        public static final int REMOVE_ENTITY_BY_TYPE = 0x10d;
        public static final int GET_BLOCK_TYPES = 0x10e;
    }

    public final class Player {
        public static final int GET_TILE = 0x201;
        public static final int SET_TILE = 0x202;
        public static final int GET_POS = 0x203;
        public static final int SET_POS = 0x204;
        public static final int GET_ENTITY_BY_TYPE = 0x206;
        public static final int REMOVE_ENTITY_BY_TYPE = 0x207;
        public static final int CLEAR_EVENTS = 0x208;
    }

    public final class Entity {
        public static final int GET_TILE = 0x301;
        public static final int SET_TILE = 0x302;
        public static final int GET_POS = 0x303;
        public static final int SET_POS = 0x304;
        public static final int CLEAR_EVENTS = 0x305;
    }

    public final class Event {
        public static final int CLEAR = 0x401;
        public static final int BLOCK_HITS = 0x402;
    }

    public final class Misc {
        public static final int CHAT = 0x501;
        public static final int COMMAND = 0x502;
    }

    public final class Camera {
        public static final int MODE_NORMAL = 0x601;
        public static final int MODE_FIXED = 0x602;
        public static final int MODE_FOLLOW = 0x603;
        public static final int SET_POS = 0x604;
    }
}
