package software.ulpgc.chess.application.model.game;

import software.ulpgc.chess.application.model.board.Piece;
import software.ulpgc.chess.application.model.position.Move;

import java.util.Optional;

public record PlayedMove(Move move, Optional<Piece> promotion) {
}
