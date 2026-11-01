package software.ulpgc.chess.infrastructure.adapters.deserializer;

import software.ulpgc.chess.application.domain.model.Position;
import software.ulpgc.chess.application.ports.deserializer.BoardDeserializer;
import software.ulpgc.chess.application.ports.deserializer.PositionDeserializer;
import software.ulpgc.chess.application.ports.deserializer.StateDeserializer;

public class FenPositionDeserializer implements PositionDeserializer {

    private final BoardDeserializer boardDeserializer;
    private final StateDeserializer stateDeserializer;

    public FenPositionDeserializer(BoardDeserializer boardDeserializer, StateDeserializer stateDeserializer) {
        this.boardDeserializer = boardDeserializer;
        this.stateDeserializer = stateDeserializer;
    }

    @Override
    public Position deserialize(String value) {
        return new Position(boardDeserializer.deserialize(value), stateDeserializer.deserialize(value));
    }
}
