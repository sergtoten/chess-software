package tests.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.domain.model.Board;
import software.ulpgc.chess.application.domain.model.Color;
import software.ulpgc.chess.application.domain.model.Move;
import software.ulpgc.chess.application.domain.model.Position;
import software.ulpgc.chess.application.domain.model.Square;
import software.ulpgc.chess.application.domain.model.State;
import software.ulpgc.chess.application.domain.service.CastlingMoveGenerator;
import software.ulpgc.chess.application.domain.service.MoveGenerate;
import software.ulpgc.chess.application.domain.service.PawnMoveGenerate;
import software.ulpgc.chess.application.domain.service.GameRules;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenPositionDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;

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

    private GameRules rules;
    private FenBoardDeserializer deserializer;
    private FenPositionDeserializer positionDeserializer;

    @BeforeEach
    void setUp() {
        rules = new GameRules(new MoveGenerate(new PawnMoveGenerate()), new CastlingMoveGenerator());
        deserializer = new FenBoardDeserializer();
        positionDeserializer = new FenPositionDeserializer(deserializer, new FenStateDeserializer());
    }

    private Move move(String from, String to) {
        return new Move(Square.at(from), Square.at(to));
    }

    private Board board(String fen) {
        return deserializer.deserialize(fen);
    }

    private List<Move> legalMovesOf(String fullFen) {
        Position position = positionDeserializer.deserialize(fullFen);
        return rules.legalMoves(position.board(), position.state().turn(), position.state());
    }

    private List<Move> legalMovesFrom(String fen, Color color, String square) {
        Square source = Square.at(square);
        return rules.legalMoves(board(fen), color, State.initial()).stream()
                .filter(move -> move.from().equals(source))
                .toList();
    }

    // ---------- Posición inicial ----------

    @Test
    @DisplayName("En la posición inicial las blancas tienen 20 movimientos legales")
    void initialPositionWhiteHasTwentyLegalMoves() {
        assertThat(rules.legalMoves(board(INITIAL), Color.White, State.initial())).hasSize(20);
    }

    @Test
    @DisplayName("En la posición inicial las negras tienen 20 movimientos legales")
    void initialPositionBlackHasTwentyLegalMoves() {
        assertThat(rules.legalMoves(board(INITIAL), Color.Black, State.initial())).hasSize(20);
    }

    @Test
    @DisplayName("La posición inicial no es mate ni ahogado")
    void initialPositionIsNeitherMateNorStalemate() {
        assertThat(rules.isCheckmate(board(INITIAL), Color.White, State.initial())).isFalse();
        assertThat(rules.isStalemate(board(INITIAL), Color.White, State.initial())).isFalse();
    }

    // ---------- Jaque mate ----------

    @Test
    @DisplayName("El mate del loco es jaque mate para las blancas")
    void foolsMateIsCheckmate() {
        assertThat(rules.isCheckmate(board(FOOLS_MATE), Color.White, State.initial())).isTrue();
    }

    @Test
    @DisplayName("En el mate del loco las blancas no tienen movimientos legales")
    void foolsMateHasNoLegalMoves() {
        assertThat(rules.legalMoves(board(FOOLS_MATE), Color.White, State.initial())).isEmpty();
    }

    @Test
    @DisplayName("El mate del loco no es rey ahogado")
    void foolsMateIsNotStalemate() {
        assertThat(rules.isStalemate(board(FOOLS_MATE), Color.White, State.initial())).isFalse();
    }

    @Test
    @DisplayName("El mate del pasillo es jaque mate para las negras")
    void backRankMateIsCheckmate() {
        assertThat(rules.isCheckmate(board(BACK_RANK_MATE), Color.Black, State.initial())).isTrue();
        assertThat(rules.isStalemate(board(BACK_RANK_MATE), Color.Black, State.initial())).isFalse();
    }

    // ---------- Rey ahogado ----------

    @Test
    @DisplayName("Sin jaque y sin movimientos legales es rey ahogado")
    void stalemateIsDetected() {
        assertThat(rules.isStalemate(board(STALEMATE), Color.Black, State.initial())).isTrue();
    }

    @Test
    @DisplayName("El rey ahogado no es jaque mate")
    void stalemateIsNotCheckmate() {
        assertThat(rules.isCheckmate(board(STALEMATE), Color.Black, State.initial())).isFalse();
    }

    @Test
    @DisplayName("En el rey ahogado las negras no tienen movimientos legales")
    void stalemateHasNoLegalMoves() {
        assertThat(rules.legalMoves(board(STALEMATE), Color.Black, State.initial())).isEmpty();
    }

    // ---------- Jaque que no es mate ----------

    @Test
    @DisplayName("Con el rey en jaque solo valen los movimientos que lo resuelven")
    void inCheckOnlyEscapesAreLegal() {
        assertThat(rules.legalMoves(board(CHECK_ROOK_ON_E4), Color.White, State.initial())).containsExactlyInAnyOrder(
                move("e1", "d1"), move("e1", "d2"), move("e1", "f1"), move("e1", "f2"));
    }

    @Test
    @DisplayName("Estar en jaque con salidas no es mate ni ahogado")
    void checkWithEscapesIsNeitherMateNorStalemate() {
        assertThat(rules.isCheckmate(board(CHECK_ROOK_ON_E4), Color.White, State.initial())).isFalse();
        assertThat(rules.isStalemate(board(CHECK_ROOK_ON_E4), Color.White, State.initial())).isFalse();
    }

    @Test
    @DisplayName("Con el rey en jaque, el movimiento que no lo resuelve se descarta")
    void movesThatIgnoreTheCheckAreDiscarded() {
        assertThat(rules.legalMoves(board(BLOCK_OR_MOVE), Color.White, State.initial())).containsExactlyInAnyOrder(
                move("e1", "d2"), move("e1", "e2"), move("e1", "f2"));
    }

    // ---------- Reglas del rey y piezas clavadas ----------

    @Test
    @DisplayName("El rey no puede moverse a una casilla atacada")
    void kingCannotMoveIntoAttackedSquare() {
        assertThat(rules.legalMoves(board(ROOK_CONTROLS_RANK_2), Color.White, State.initial())).containsExactlyInAnyOrder(
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
        assertThat(rules.legalMoves(board(PINNED_KNIGHT), Color.White, State.initial())).containsExactlyInAnyOrder(
                move("e1", "d1"), move("e1", "d2"), move("e1", "f1"), move("e1", "f2"));
    }

    // ---------- Independencia del tablero original ----------

    @Test
    @DisplayName("Calcular los movimientos legales no modifica el tablero")
    void legalMovesDoNotModifyTheBoard() {
        Board original = board(INITIAL);
        rules.legalMoves(original, Color.White, State.initial());
        assertThat(original.equals(board(INITIAL))).isTrue();
    }

    // ---------- Jaque ----------

    @Test
    @DisplayName("La posición inicial no está en jaque para ningún color")
    void initialPositionIsNotCheck() {
        assertThat(rules.isCheck(board(INITIAL), Color.White)).isFalse();
        assertThat(rules.isCheck(board(INITIAL), Color.Black)).isFalse();
    }

    @Test
    @DisplayName("En el mate del loco las blancas están en jaque")
    void foolsMateIsCheck() {
        assertThat(rules.isCheck(board(FOOLS_MATE), Color.White)).isTrue();
    }

    @Test
    @DisplayName("En el mate del loco las negras no están en jaque")
    void foolsMateBlackIsNotCheck() {
        assertThat(rules.isCheck(board(FOOLS_MATE), Color.Black)).isFalse();
    }

    @Test
    @DisplayName("Una torre enemiga en la misma columna da jaque")
    void rookOnTheSameFileIsCheck() {
        assertThat(rules.isCheck(board(CHECK_ROOK_ON_E4), Color.White)).isTrue();
    }

    @Test
    @DisplayName("El mate del pasillo deja a las negras en jaque")
    void backRankMateBlackIsCheck() {
        assertThat(rules.isCheck(board(BACK_RANK_MATE), Color.Black)).isTrue();
    }

    @Test
    @DisplayName("El rey ahogado no está en jaque")
    void stalemateIsNotCheck() {
        assertThat(rules.isCheck(board(STALEMATE), Color.Black)).isFalse();
    }

    @Test
    @DisplayName("Una pieza que bloquea el ataque evita el jaque")
    void blockedAttackIsNotCheck() {
        assertThat(rules.isCheck(board("4r1k1/8/8/8/8/8/4N3/4K3"), Color.White)).isFalse();
    }

    // ---------- Enroque ----------

    @Test
    @DisplayName("Las blancas pueden enrocar en corto y en largo si el camino está libre")
    void whiteCanCastleBothSides() {
        assertThat(legalMovesOf("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"))
                .contains(move("e1", "g1"), move("e1", "c1"));
    }

    @Test
    @DisplayName("Las negras pueden enrocar en corto y en largo si el camino está libre")
    void blackCanCastleBothSides() {
        assertThat(legalMovesOf("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1"))
                .contains(move("e8", "g8"), move("e8", "c8"));
    }

    @Test
    @DisplayName("No se puede enrocar sin derechos de enroque")
    void noCastlingWithoutRights() {
        assertThat(legalMovesOf("r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1"))
                .doesNotContain(move("e1", "g1"), move("e1", "c1"));
    }

    @Test
    @DisplayName("Solo se puede enrocar por el lado que conserva el derecho")
    void onlyTheSideWithRight() {
        assertThat(legalMovesOf("r3k2r/8/8/8/8/8/8/R3K2R w Kkq - 0 1"))
                .contains(move("e1", "g1"));
        assertThat(legalMovesOf("r3k2r/8/8/8/8/8/8/R3K2R w Kkq - 0 1"))
                .doesNotContain(move("e1", "c1"));
    }

    @Test
    @DisplayName("No se puede enrocar si hay una pieza entre el rey y la torre")
    void noCastlingWithPieceInBetween() {
        assertThat(legalMovesOf("r3k2r/8/8/8/8/8/8/R3KB1R w KQkq - 0 1"))
                .doesNotContain(move("e1", "g1"));
        assertThat(legalMovesOf("r3k2r/8/8/8/8/8/8/RN2K2R w KQkq - 0 1"))
                .doesNotContain(move("e1", "c1"));
    }

    @Test
    @DisplayName("No se puede enrocar con el rey en jaque")
    void noCastlingWhileInCheck() {
        assertThat(legalMovesOf("4r2k/8/8/8/8/8/8/R3K3 w Q - 0 1"))
                .doesNotContain(move("e1", "c1"));
    }

    @Test
    @DisplayName("No se puede enrocar si el rey cruza una casilla atacada")
    void noCastlingThroughAttackedSquare() {
        assertThat(legalMovesOf("3r3k/8/8/8/8/8/8/R3K3 w Q - 0 1"))
                .doesNotContain(move("e1", "c1"));
        assertThat(legalMovesOf("5rk1/8/8/8/8/8/8/4K2R w K - 0 1"))
                .doesNotContain(move("e1", "g1"));
    }

    @Test
    @DisplayName("No se puede enrocar si el rey acaba en una casilla atacada")
    void noCastlingIntoAttackedSquare() {
        assertThat(legalMovesOf("6rk/8/8/8/8/8/8/4K2R w K - 0 1"))
                .doesNotContain(move("e1", "g1"));
    }

    @Test
    @DisplayName("El enroque largo es legal aunque b1 esté atacada, porque el rey no la cruza")
    void longCastlingIsLegalWhenOnlyB1IsAttacked() {
        assertThat(legalMovesOf("1r5k/8/8/8/8/8/8/R3K3 w Q - 0 1"))
                .contains(move("e1", "c1"));
    }

    @Test
    @DisplayName("Las negras no pueden enrocar largo si d8 está atacada")
    void blackNoCastlingThroughAttackedSquare() {
        assertThat(legalMovesOf("r3k2r/8/8/8/8/8/3R4/4K3 b kq - 0 1"))
                .doesNotContain(move("e8", "c8"));
    }
}