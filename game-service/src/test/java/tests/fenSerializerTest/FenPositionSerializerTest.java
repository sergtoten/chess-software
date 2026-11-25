package tests.fenSerializerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.model.board.Board;
import software.ulpgc.chess.application.model.position.CastlingRight;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Piece;
import software.ulpgc.chess.application.model.position.Position;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.application.model.position.State;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenPositionDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenBoardSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenPositionSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenStateSerializer;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FenPositionSerializerTest {

    private FenPositionSerializer serializer;
    private FenPositionDeserializer deserializer;

    @BeforeEach
    void setUp() {
        serializer = new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer());
        deserializer = new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer());
    }

    @Test
    @DisplayName("Debe serializar la posición inicial completa")
    void shouldSerializeInitialPosition() {
        Position position = new Position(Board.initial(), State.initial());

        assertThat(serializer.serialize(position))
                .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    @Test
    @DisplayName("Debe serializar una posición personalizada con tablero y estado")
    void shouldSerializeCustomPosition() {
        Board board = new Board(Map.of(
                Square.at("e1"), Piece.WhiteKing,
                Square.at("e8"), Piece.BlackKing));
        State state = new State(
                Color.Black,
                Set.of(CastlingRight.WhiteShort, CastlingRight.BlackLong),
                Optional.of(Square.at("e3")),
                5, 12);

        assertThat(serializer.serialize(new Position(board, state)))
                .isEqualTo("4k3/8/8/8/8/8/8/4K3 b Kq e3 5 12");
    }

    @Test
    @DisplayName("Debe separar el tablero y el estado con un único espacio, sin espacios en los extremos")
    void shouldJoinBoardAndStateWithSingleSpace() {
        FenPositionSerializer withDoubles = new FenPositionSerializer(board -> "TABLERO", state -> "ESTADO");

        String fen = withDoubles.serialize(new Position(Board.initial(), State.initial()));

        assertThat(fen).isEqualTo("TABLERO ESTADO");
    }

    @Test
    @DisplayName("Debe entregar a cada serializador solo su parte de la posición")
    void shouldGiveEachSerializerOnlyItsPart() {
        Board board = Board.initial();
        State state = State.initial();
        Object[] receivedByBoard = new Object[1];
        Object[] receivedByState = new Object[1];
        FenPositionSerializer withDoubles = new FenPositionSerializer(
                value -> { receivedByBoard[0] = value; return "b"; },
                value -> { receivedByState[0] = value; return "s"; });

        withDoubles.serialize(new Position(board, state));

        assertThat(receivedByBoard[0]).isSameAs(board);
        assertThat(receivedByState[0]).isSameAs(state);
    }

    @Test
    @DisplayName("Debe recuperar el FEN original tras deserializar y volver a serializar")
    void shouldRoundTripWithDeserializer() {
        String[] fens = {
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "4k3/8/8/8/8/8/8/4K3 b Kq e3 5 12",
                "8/8/8/8/8/8/8/8 w - - 0 1",
                "r2n3k/8/8/8/8/8/8/R3K2R w KQ - 17 24"
        };

        for (String fen : fens)
            assertThat(serializer.serialize(deserializer.deserialize(fen))).isEqualTo(fen);
    }
}