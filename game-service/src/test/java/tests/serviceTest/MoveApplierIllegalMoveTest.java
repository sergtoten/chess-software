package tests.serviceTest;

import java.util.Optional;
import software.ulpgc.chess.application.model.board.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.domain.IllegalMoveException;
import software.ulpgc.chess.application.model.position.Move;
import software.ulpgc.chess.application.model.position.Position;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.application.domain.service.CastlingMoveGenerator;
import software.ulpgc.chess.application.domain.service.GameRules;
import software.ulpgc.chess.application.domain.service.MoveApplier;
import software.ulpgc.chess.application.domain.service.MoveGenerate;
import software.ulpgc.chess.application.domain.service.PawnMoveGenerate;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenPositionDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoveApplierIllegalMoveTest {

    private static final String INITIAL = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private MoveApplier applier;
    private FenPositionDeserializer deserializer;

    @BeforeEach
    void setUp() {
        applier = new MoveApplier(new GameRules(new MoveGenerate(new PawnMoveGenerate()), new CastlingMoveGenerator()));
        deserializer = new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer());
    }

    private Position apply(String fen, String from, String to) {
        return applier.applyMove(deserializer.deserialize(fen), new Move(Square.at(from), Square.at(to)));
    }

    @Test
    @DisplayName("Mover una pieza del color que no tiene el turno lanza IllegalMoveException")
    void wrongTurnThrows() {
        assertThatThrownBy(() -> apply(INITIAL, "e7", "e5")).isInstanceOf(IllegalMoveException.class);
    }

    @Test
    @DisplayName("Un movimiento que la pieza no puede hacer lanza IllegalMoveException")
    void impossibleMoveThrows() {
        assertThatThrownBy(() -> apply(INITIAL, "a1", "a3")).isInstanceOf(IllegalMoveException.class);
    }

    @Test
    @DisplayName("Un movimiento que deja al propio rey en jaque lanza IllegalMoveException")
    void moveLeavingOwnKingInCheckThrows() {
        assertThatThrownBy(() -> apply("4r1k1/8/8/8/8/8/4N3/4K3 w - - 0 1", "e2", "c3"))
                .isInstanceOf(IllegalMoveException.class);
    }

    @Test
    @DisplayName("Mover desde una casilla vacía lanza IllegalMoveException")
    void moveFromEmptySquareThrows() {
        assertThatThrownBy(() -> apply(INITIAL, "e4", "e5")).isInstanceOf(IllegalMoveException.class);
    }

    @Test
    @DisplayName("Enrocar con el rey en jaque lanza IllegalMoveException")
    void castlingWhileInCheckThrows() {
        assertThatThrownBy(() -> apply("4r2k/8/8/8/8/8/8/R3K3 w Q - 0 1", "e1", "c1"))
                .isInstanceOf(IllegalMoveException.class);
    }

    @Test
    @DisplayName("Enrocar sin derechos lanza IllegalMoveException")
    void castlingWithoutRightsThrows() {
        assertThatThrownBy(() -> apply("r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1", "e1", "g1"))
                .isInstanceOf(IllegalMoveException.class);
    }

    @Test
    @DisplayName("Enrocar con una pieza en medio lanza IllegalMoveException")
    void castlingThroughAPieceThrows() {
        assertThatThrownBy(() -> apply("r3k2r/8/8/8/8/8/8/R3KB1R w KQkq - 0 1", "e1", "g1"))
                .isInstanceOf(IllegalMoveException.class);
    }

    // ---------- Promoción inválida ----------

    private Position applyPromoting(String from, String to, Piece piece) {
        return applier.applyMove(deserializer.deserialize("7k/4P3/8/8/8/8/8/4K3 w - - 0 1"),
                new Move(Square.at(from), Square.at(to)), Optional.of(piece));
    }

    @Test
    @DisplayName("Llegar a la última fila sin elegir pieza lanza IllegalMoveException")
    void promotionWithoutPieceThrows() {
        assertThatThrownBy(() -> apply("7k/4P3/8/8/8/8/8/4K3 w - - 0 1", "e7", "e8"))
                .isInstanceOf(IllegalMoveException.class);
    }

    @Test
    @DisplayName("Promocionar a rey lanza IllegalMoveException")
    void promotionToKingThrows() {
        assertThatThrownBy(() -> applyPromoting("e7", "e8", Piece.WhiteKing))
                .isInstanceOf(IllegalMoveException.class);
    }

    @Test
    @DisplayName("Promocionar a peón lanza IllegalMoveException")
    void promotionToPawnThrows() {
        assertThatThrownBy(() -> applyPromoting("e7", "e8", Piece.WhitePawn))
                .isInstanceOf(IllegalMoveException.class);
    }

    @Test
    @DisplayName("Promocionar a una pieza del color contrario lanza IllegalMoveException")
    void promotionToOpponentPieceThrows() {
        assertThatThrownBy(() -> applyPromoting("e7", "e8", Piece.BlackQueen))
                .isInstanceOf(IllegalMoveException.class);
    }
}
