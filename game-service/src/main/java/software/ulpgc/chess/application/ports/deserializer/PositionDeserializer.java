package software.ulpgc.chess.application.ports.deserializer;

import software.ulpgc.chess.application.model.position.Position;

public interface PositionDeserializer {

    Position deserialize(String value);

}
