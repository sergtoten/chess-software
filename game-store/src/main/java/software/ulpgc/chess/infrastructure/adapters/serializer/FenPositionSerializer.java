package software.ulpgc.chess.infrastructure.adapters.serializer;

import software.ulpgc.chess.application.domain.model.Position;
import software.ulpgc.chess.application.ports.serializer.BoardSerializer;
import software.ulpgc.chess.application.ports.serializer.PositionSerializer;
import software.ulpgc.chess.application.ports.serializer.StateSerializer;

public class FenPositionSerializer implements PositionSerializer {

    private final BoardSerializer boardSerializer;
    private final StateSerializer stateSerializer;

    public FenPositionSerializer(BoardSerializer boardSerializer, StateSerializer stateSerializer) {
        this.boardSerializer = boardSerializer;
        this.stateSerializer = stateSerializer;
    }

    @Override
    public String serialize(Position position) {
        return boardSerializer.serialize(position.board()) + " " + stateSerializer.serialize(position.state());
    }
}
