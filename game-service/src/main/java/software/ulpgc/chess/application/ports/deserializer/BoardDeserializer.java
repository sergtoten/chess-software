package software.ulpgc.chess.application.ports.deserializer;

import software.ulpgc.chess.application.model.board.Board;

public interface BoardDeserializer {
    Board deserialize(String value);
}
