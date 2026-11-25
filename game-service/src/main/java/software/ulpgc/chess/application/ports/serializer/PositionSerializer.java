package software.ulpgc.chess.application.ports.serializer;

import software.ulpgc.chess.application.model.position.Position;

public interface PositionSerializer {
    String serialize(Position position);
}
