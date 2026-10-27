package software.ulpgc.chess.apliccation.ports;

import software.ulpgc.chess.domain.model.Board;

public interface BoardDeserializer {
    Board deserialize(String value);
}
