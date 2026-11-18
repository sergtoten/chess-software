package tests.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.Move;
import software.ulpgc.chess.application.model.Position;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.application.domain.service.CastlingMoveGenerator;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenPositionDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CastlingMoveGeneratorTest {

    private CastlingMoveGenerator generator;
    private FenPositionDeserializer deserializer;

    @BeforeEach
    void setUp() {
        generator = new CastlingMoveGenerator();
        deserializer = new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer());
    }

    private Move move(String from, String to) {
        return new Move(Square.at(from), Square.at(to));
    }

    private List<Move> castlingMoves(String fen, Color color) {
        Position position = deserializer.deserialize(fen);
        return generator.castlingMoves(position.board(), color, position.state());
    }

    @Test
    @DisplayName("Las blancas generan el enroque corto y el largo si tienen derechos y el camino libre")
    void whiteGeneratesBothCastlings() {
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", Color.White))
                .containsExactlyInAnyOrder(move("e1", "g1"), move("e1", "c1"));
    }

    @Test
    @DisplayName("Las negras generan sus enroques en la fila 8")
    void blackGeneratesBothCastlingsOnRankEight() {
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1", Color.Black))
                .containsExactlyInAnyOrder(move("e8", "g8"), move("e8", "c8"));
    }

    @Test
    @DisplayName("En la posición inicial no hay enroques")
    void noCastlingInInitialPosition() {
        assertThat(castlingMoves("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", Color.White)).isEmpty();
    }

    @Test
    @DisplayName("Sin derechos de enroque no se genera ninguno")
    void noRightsNoCastling() {
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1", Color.White)).isEmpty();
    }

    @Test
    @DisplayName("Solo se genera el enroque del lado que conserva el derecho")
    void onlyTheSideWithRightIsGenerated() {
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/R3K2R w Kkq - 0 1", Color.White))
                .containsExactlyInAnyOrder(move("e1", "g1"));
    }

    @Test
    @DisplayName("Una pieza en f1 impide el enroque corto pero no el largo")
    void pieceOnF1BlocksOnlyShortCastling() {
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/R3KB1R w KQkq - 0 1", Color.White))
                .containsExactlyInAnyOrder(move("e1", "c1"));
    }

    @Test
    @DisplayName("Una pieza en b1 impide el enroque largo pero no el corto")
    void pieceOnB1BlocksOnlyLongCastling() {
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/RN2K2R w KQkq - 0 1", Color.White))
                .containsExactlyInAnyOrder(move("e1", "g1"));
    }

    @Test
    @DisplayName("Si falta la torre no se genera el enroque de ese lado")
    void missingRookNoCastling() {
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/4K2R w KQkq - 0 1", Color.White))
                .containsExactlyInAnyOrder(move("e1", "g1"));
    }

    @Test
    @DisplayName("Si el rey no está en su casilla inicial no se genera ningún enroque")
    void kingNotOnItsSquareNoCastling() {
        assertThat(castlingMoves("r3k2r/8/8/8/8/8/8/R2K3R w KQkq - 0 1", Color.White)).isEmpty();
    }

    @Test
    @DisplayName("No se generan enroques del color que no tiene el rey en e1")
    void otherColorDoesNotGetWhiteCastlings() {
        assertThat(castlingMoves("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1", Color.Black)).isEmpty();
    }
}