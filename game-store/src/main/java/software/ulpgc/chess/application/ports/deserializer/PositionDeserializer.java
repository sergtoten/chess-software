package software.ulpgc.chess.application.ports.deserializer;

import software.ulpgc.chess.application.model.Position;

public interface PositionDeserializer {

    Position deserialize(String position);

}
