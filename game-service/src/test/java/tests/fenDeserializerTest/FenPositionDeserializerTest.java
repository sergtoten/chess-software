package tests.fenDeserializerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.model.Board;
import software.ulpgc.chess.application.model.CastlingRight;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Piece;
import software.ulpgc.chess.application.model.Position;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.application.model.State;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenPositionDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FenPositionDeserializerTest {

    private static final String INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private FenPositionDeserializer deserializer;

    @BeforeEach
    void setUp() {
        deserializer = new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer());
    }

    @Test
    @DisplayName("Debe deserializar la posición inicial completa")
    void shouldDeserializeInitialPosition() {
        Position position = deserializer.deserialize(INITIAL_FEN);

        assertThat(position.board()).isEqualTo(Board.initial());
        assertThat(position.state()).isEqualTo(State.initial());
    }

    @Test
    @DisplayName("Debe deserializar a la vez el tablero y el estado de una posición personalizada")
    void shouldDeserializeCustomPosition() {
        Position position = deserializer.deserialize("4k3/8/8/8/8/8/8/4K3 b Kq e3 5 12");

        assertThat(position.board()).isEqualTo(new Board(Map.of(
                Square.at("e1"), Piece.WhiteKing,
                Square.at("e8"), Piece.BlackKing)));
        assertThat(position.state()).isEqualTo(new State(
                Color.Black,
                Set.of(CastlingRight.WhiteShort, CastlingRight.BlackLong),
                Optional.of(Square.at("e3")),
                5, 12));
    }

    @Test
    @DisplayName("Debe deserializar una posición con espacios adicionales entre los campos")
    void shouldAcceptExtraSpaces() {
        Position position = deserializer.deserialize("8/8/8/8/8/8/8/8   b   -   -   3   7");

        assertThat(position.board()).isEqualTo(new Board(Map.of()));
        assertThat(position.state()).isEqualTo(
                new State(Color.Black, Set.of(), Optional.empty(), 3, 7));
    }

    @Test
    @DisplayName("Debe pasar el FEN completo, sin trocear, a los dos deserializadores")
    void shouldDelegateFullFenToBothDeserializers() {
        String[] receivedByBoard = new String[1];
        String[] receivedByState = new String[1];
        FenPositionDeserializer withDoubles = new FenPositionDeserializer(
                value -> { receivedByBoard[0] = value; return Board.initial(); },
                value -> { receivedByState[0] = value; return State.initial(); });

        withDoubles.deserialize(INITIAL_FEN);

        assertThat(receivedByBoard[0]).isEqualTo(INITIAL_FEN);
        assertThat(receivedByState[0]).isEqualTo(INITIAL_FEN);
    }

    @Test
    @DisplayName("Debe construir la posición con exactamente lo que devuelven los deserializadores")
    void shouldBuildPositionFromWhatDeserializersReturn() {
        Board board = new Board(Map.of(Square.at("a1"), Piece.WhiteRook));
        State state = new State(Color.Black, Set.of(), Optional.empty(), 9, 9);
        FenPositionDeserializer withDoubles = new FenPositionDeserializer(value -> board, value -> state);

        Position position = withDoubles.deserialize("cualquier texto");

        assertThat(position.board()).isSameAs(board);
        assertThat(position.state()).isSameAs(state);
    }
}