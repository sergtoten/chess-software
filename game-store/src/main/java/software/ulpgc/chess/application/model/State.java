package software.ulpgc.chess.application.model;

import java.util.EnumSet;
import java.util.Optional;

public record State(
        Color turn,
        EnumSet<CastlingRight> castlings,
        Optional<Square> onPassant,
        int halfMoveClock,
        int fullMoveNumber
) {
}
