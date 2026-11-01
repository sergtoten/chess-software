package software.ulpgc.chess.application.ports.serializer;

import software.ulpgc.chess.application.domain.model.Position;

public interface PositionSerializer {
    String serialize(Position position);
}
