package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.model.position.Move;
import software.ulpgc.chess.application.model.board.Square;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

final class MoveReader {

    static final String MOVE = "move";
    static final String PROMOTION = "promotion";
    private static final Pattern MOVE_FORMAT = Pattern.compile("[a-h][1-8][a-h][1-8]");
    private static final Set<String> PROMOTION_KINDS = Set.of("Queen", "Rook", "Bishop", "Knight");

    record MoveRequest(Move move, Optional<String> promotionKind) {}

    static Optional<MoveRequest> from(UseCase.Input input) {
        String move = input.get(MOVE);
        if (move == null || !MOVE_FORMAT.matcher(move).matches()) return Optional.empty();
        String promotion = input.get(PROMOTION);
        if (promotion != null && !PROMOTION_KINDS.contains(promotion)) return Optional.empty();
        Move parsed = new Move(Square.at(move.substring(0, 2)), Square.at(move.substring(2, 4)));
        return Optional.of(new MoveRequest(parsed, Optional.ofNullable(promotion)));
    }
}
