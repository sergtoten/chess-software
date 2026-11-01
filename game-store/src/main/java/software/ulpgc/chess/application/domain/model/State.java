package software.ulpgc.chess.application.domain.model;

import java.util.Optional;
import java.util.Set;

public record State(
        Color turn,
        Set<CastlingRight> castlings,
        Optional<Square> onPassant,
        int halfMoveClock,
        int fullMoveNumber
) {

    public State {
        castlings = Set.copyOf(castlings);
    }

    public static State initial() {
        return new State(Color.White, Set.of(CastlingRight.values()), Optional.empty(), 0, 1);
    }

}
