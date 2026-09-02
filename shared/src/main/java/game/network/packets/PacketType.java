package game.network.packets;

public enum PacketType {

    // Lobby
    LOBBY_STATE,
    PLAYER_JOINED,
    PLAYER_LEFT,
    PLAYER_READY,
    PLAYER_UNREADY,

    // Connection
    PING,
    PONG,

    // Game
    START_GAME,
    GAME_STARTING
}