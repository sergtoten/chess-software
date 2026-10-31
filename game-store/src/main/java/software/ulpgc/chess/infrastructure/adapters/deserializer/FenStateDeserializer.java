package software.ulpgc.chess.infrastructure.adapters.deserializer;

import software.ulpgc.chess.application.model.CastlingRight;
import software.ulpgc.chess.application.model.Color;
import software.ulpgc.chess.application.model.Square;
import software.ulpgc.chess.application.model.State;
import software.ulpgc.chess.application.ports.deserializer.StateDeserializer;

import java.util.*;

public class FenStateDeserializer implements StateDeserializer {

    private static final Map<Character, Color> TURN_COLORS = Map.ofEntries(
            Map.entry('w', Color.White), Map.entry('b', Color.Black)
    );

    private static final Map<Character, CastlingRight> CASTLING_RIGHT = Map.ofEntries(
            Map.entry('K', CastlingRight.WhiteShort), Map.entry('q', CastlingRight.BlackLong),
            Map.entry('k', CastlingRight.BlackShort), Map.entry('Q',  CastlingRight.WhiteLong)
    );

    @Override
    public State deserialize(String value) {
        return deserialize(value.trim().split("\\s+"));
    }

    private State deserialize(String[] split) {
        return new State(colorTurn(split[1].charAt(0)), castlingRight(split[2]), onPassant(split[3]), halfMove(split[4]), fullmove(split[5]));
    }

    private int fullmove(String s) {
        return Integer.parseInt(s);
    }

    private int halfMove(String s) {
        return Integer.parseInt(s);
    }

    private Optional<Square> onPassant(String s) {
        if (s.equals("-")) {return Optional.empty();}
        return Optional.of(Square.at(s));
    }

    private Set<CastlingRight> castlingRight(String s) {
        Set<CastlingRight> result = new HashSet<>();
        if (s.equals("-")) return result;
        for (char c : s.toCharArray())
            result.add(castlingRight(c));
        return result;
    }

    private CastlingRight castlingRight(char c) {
        return CASTLING_RIGHT.get(c);
    }

    private Color colorTurn(char turn) {
        return TURN_COLORS.get(turn);
    }
}
