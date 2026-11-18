package software.ulpgc.chess.infrastructure.adapters.serializer;

import software.ulpgc.chess.application.model.CastlingRight;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.application.model.State;
import software.ulpgc.chess.application.ports.serializer.StateSerializer;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FenStateSerializer implements StateSerializer {

    private static final Map<Color, Character> TURN_COLORS = Map.ofEntries(
            Map.entry(Color.White, 'w'), Map.entry(Color.Black, 'b')
    );

    private static final Map<CastlingRight, Character>  CASTLING_RIGHTS = Map.ofEntries(
            Map.entry(CastlingRight.WhiteShort, 'K'), Map.entry(CastlingRight.BlackShort, 'k'),
            Map.entry(CastlingRight.WhiteLong, 'Q'), Map.entry(CastlingRight.BlackLong, 'q')
    );

    @Override
    public String serialize(State state) {
        return fields(state.turn(), state.castlings(), state.onPassant(), state.halfMoveClock(), state.fullMoveNumber());
    }

    private String fields(Color turn, Set<CastlingRight> castlings, Optional<Square> onPassant, int halfmove, int fullmove) {
        String fields = colorTurn(turn) + " " + castlingRight(castlings) + " " + passant(onPassant) + " " + half(halfmove) + " " + full(fullmove);
        return fields;
    }

    private String full(int fullmove) {
        return Integer.toString(fullmove);
    }

    private String half(int halfmove) {
        return Integer.toString(halfmove);
    }

    private String passant(Optional<Square> onPassant) {
        return onPassant.stream().map(this::square).findFirst().orElse("-");
    }

    private String square(Square square) {
        return "" + square.file().symbol() + square.rank().symbol();
    }

    private String castlingRight(Set<CastlingRight> castlings) {
        if (castlings.isEmpty()) return "-";
        return Arrays.stream(CastlingRight.values())
                .filter(castlings::contains)
                .map(CASTLING_RIGHTS::get)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
    private String colorTurn(Color turn) {
        return TURN_COLORS.get(turn).toString();
    }
}
