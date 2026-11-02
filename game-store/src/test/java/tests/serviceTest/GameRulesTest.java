package tests.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.domain.model.Board;
import software.ulpgc.chess.application.domain.model.Color;
import software.ulpgc.chess.application.domain.model.Move;
import software.ulpgc.chess.application.domain.model.Square;
import software.ulpgc.chess.application.domain.service.MoveGenerate;
import software.ulpgc.chess.application.domain.service.GameRules;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameRulesTest {

    private static final String INITIAL = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    private static final String FOOLS_MATE = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR";
    private static final String STALEMATE = "7k/5Q2/6K1/8/8/8/8/8";
    private static final String BACK_RANK_MATE = "R5k1/5ppp/8/8/8/8/8/4K3";
    private static final String CHECK_ROOK_ON_E4 = "4k3/8/8/8/4r3/8/8/4K3";
    private static final String PINNED_KNIGHT = "4r1k1/8/8/8/8/8/4N3/4K3";
    private static final String ROOK_CONTROLS_RANK_2 = "4k3/8/8/8/8/8/r7/4K3";
    private static final String BLOCK_OR_MOVE = "4k3/8/8/8/8/8/8/R3K2r";

    private GameRules validator;
    private FenBoardDeserializer deserializer;

    @BeforeEach
    void setUp() {
        validator = new GameRules(new MoveGenerate());
        deserializer = new FenBoardDeserializer();
    }

    private Move move(String from, String to) {
        return new Move(Square.at(from), Square.at(to));
    }

    private Board board(String fen) {
        return deserializer.deserialize(fen);
    }

    private List<Move> legalMovesFrom(String fen, Color color, String square) {
        Square source = Square.at(square);
        return validator.legalMove(board(fen), color).stream()
                .filter(move -> move.from().equals(source))
                .toList();
    }

    // ---------- Posición inicial ----------

    @Test
    @DisplayName("En la posición inicial las blancas tienen 20 movimientos legales")
    void initialPositionWhiteHasTwentyLegalMoves() {
        assertThat(validator.legalMove(board(INITIAL), Color.White)).hasSize(20);
    }

    @Test
    @DisplayName("En la posición inicial las negras tienen 20 movimientos legales")
    void initialPositionBlackHasTwentyLegalMoves() {
        assertThat(validator.legalMove(board(INITIAL), Color.Black)).hasSize(20);
    }

    @Test
    @DisplayName("La posición inicial no es mate ni ahogado")
    void initialPositionIsNeitherMateNorStalemate() {
        assertThat(validator.isCheckmate(board(INITIAL), Color.White)).isFalse();
        assertThat(validator.isStalemate(board(INITIAL), Color.White)).isFalse();
    }

    // ---------- Jaque mate ----------

    @Test
    @DisplayName("El mate del loco es jaque mate para las blancas")
    void foolsMateIsCheckmate() {
        assertThat(validator.isCheckmate(board(FOOLS_MATE), Color.White)).isTrue();
    }

    @Test
    @DisplayName("En el mate del loco las blancas no tienen movimientos legales")
    void foolsMateHasNoLegalMoves() {
        assertThat(validator.legalMove(board(FOOLS_MATE), Color.White)).isEmpty();
    }

    @Test
    @DisplayName("El mate del loco no es rey ahogado")
    void foolsMateIsNotStalemate() {
        assertThat(validator.isStalemate(board(FOOLS_MATE), Color.White)).isFalse();
    }

    @Test
    @DisplayName("El mate del pasillo es jaque mate para las negras")
    void backRankMateIsCheckmate() {
        assertThat(validator.isCheckmate(board(BACK_RANK_MATE), Color.Black)).isTrue();
        assertThat(validator.isStalemate(board(BACK_RANK_MATE), Color.Black)).isFalse();
    }

    // ---------- Rey ahogado ----------

    @Test
    @DisplayName("Sin jaque y sin movimientos legales es rey ahogado")
    void stalemateIsDetected() {
        assertThat(validator.isStalemate(board(STALEMATE), Color.Black)).isTrue();
    }

    @Test
    @DisplayName("El rey ahogado no es jaque mate")
    void stalemateIsNotCheckmate() {
        assertThat(validator.isCheckmate(board(STALEMATE), Color.Black)).isFalse();
    }

    @Test
    @DisplayName("En el rey ahogado las negras no tienen movimientos legales")
    void stalemateHasNoLegalMoves() {
        assertThat(validator.legalMove(board(STALEMATE), Color.Black)).isEmpty();
    }

    // ---------- Jaque que no es mate ----------

    @Test
    @DisplayName("Con el rey en jaque solo valen los movimientos que lo resuelven")
    void inCheckOnlyEscapesAreLegal() {
        assertThat(validator.legalMove(board(CHECK_ROOK_ON_E4), Color.White)).containsExactlyInAnyOrder(
                move("e1", "d1"), move("e1", "d2"), move("e1", "f1"), move("e1", "f2"));
    }

    @Test
    @DisplayName("Estar en jaque con salidas no es mate ni ahogado")
    void checkWithEscapesIsNeitherMateNorStalemate() {
        assertThat(validator.isCheckmate(board(CHECK_ROOK_ON_E4), Color.White)).isFalse();
        assertThat(validator.isStalemate(board(CHECK_ROOK_ON_E4), Color.White)).isFalse();
    }

    @Test
    @DisplayName("Con el rey en jaque, el movimiento que no lo resuelve se descarta")
    void movesThatIgnoreTheCheckAreDiscarded() {
        assertThat(validator.legalMove(board(BLOCK_OR_MOVE), Color.White)).containsExactlyInAnyOrder(
                move("e1", "d2"), move("e1", "e2"), move("e1", "f2"));
    }

    // ---------- Reglas del rey y piezas clavadas ----------

    @Test
    @DisplayName("El rey no puede moverse a una casilla atacada")
    void kingCannotMoveIntoAttackedSquare() {
        assertThat(validator.legalMove(board(ROOK_CONTROLS_RANK_2), Color.White)).containsExactlyInAnyOrder(
                move("e1", "d1"), move("e1", "f1"));
    }

    @Test
    @DisplayName("Una pieza clavada no puede moverse")
    void pinnedPieceCannotMove() {
        assertThat(legalMovesFrom(PINNED_KNIGHT, Color.White, "e2")).isEmpty();
    }

    @Test
    @DisplayName("Con una pieza clavada, el rey sigue pudiendo moverse")
    void kingCanStillMoveWhenAPieceIsPinned() {
        assertThat(validator.legalMove(board(PINNED_KNIGHT), Color.White)).containsExactlyInAnyOrder(
                move("e1", "d1"), move("e1", "d2"), move("e1", "f1"), move("e1", "f2"));
    }

    // ---------- Independencia del tablero original ----------

    @Test
    @DisplayName("Calcular los movimientos legales no modifica el tablero")
    void legalMovesDoNotModifyTheBoard() {
        Board original = board(INITIAL);
        validator.legalMove(original, Color.White);
        assertThat(original.equals(board(INITIAL))).isTrue();
    }
}