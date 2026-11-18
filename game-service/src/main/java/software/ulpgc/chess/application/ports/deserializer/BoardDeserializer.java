package software.ulpgc.chess.application.ports.deserializer;

import software.ulpgc.chess.application.model.Board;

public interface BoardDeserializer {
    Board deserialize(String value);
}
