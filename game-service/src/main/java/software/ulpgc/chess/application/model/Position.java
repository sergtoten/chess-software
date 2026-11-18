package software.ulpgc.chess.application.model;

import software.ulpgc.chess.application.model.board.Color;

public record Position(Board board, State state) {

    public static Position initial() {
        return new Position(Board.initial(), State.initial());
    }

    public Color turn() {
        return state.turn();
    }

}
