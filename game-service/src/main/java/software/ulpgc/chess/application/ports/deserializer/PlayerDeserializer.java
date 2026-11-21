package software.ulpgc.chess.application.ports.deserializer;

import software.ulpgc.chess.application.model.Player;

public interface PlayerDeserializer {
    Player deserialize(String data);
}
