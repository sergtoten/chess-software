package software.ulpgc.chess.application.ports.deserializer;

import software.ulpgc.chess.application.domain.model.Position;

public interface PositionDeserializer {

    Position deserialize(String value);

}
