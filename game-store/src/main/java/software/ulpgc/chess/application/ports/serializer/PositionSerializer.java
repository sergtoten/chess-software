package software.ulpgc.chess.application.ports.serializer;

import software.ulpgc.chess.application.model.Board;
import software.ulpgc.chess.application.model.Position;

public interface PositionSerializer {
    String serialize(Position position);
}
