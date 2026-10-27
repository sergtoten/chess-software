package software.ulpgc.chess.apliccation.ports;

import software.ulpgc.chess.apliccation.domain.model.Board;

public interface BoardDeserializer {
    Board deserialize(String value);
}
