package software.ulpgc.chess.application.ports.deserializer;

import software.ulpgc.chess.application.model.game.Game;

public interface GameDeserializer {
    Game deserialize(String data);

}
