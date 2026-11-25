package software.ulpgc.chess.application.model.position;

import software.ulpgc.chess.application.model.board.Board;
import software.ulpgc.chess.application.model.board.Color;

public record Position(Board board, State state) {

    public static Position initial() {
        return new Position(Board.initial(), State.initial());
    }

    public Color turn() {
        return state.turn();
    }

}
