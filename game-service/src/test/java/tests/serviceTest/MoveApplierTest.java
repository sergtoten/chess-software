package tests.serviceTest;

import java.util.Optional;
import software.ulpgc.chess.application.model.board.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.model.position.Move;
import software.ulpgc.chess.application.model.position.Position;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.application.domain.service.GameRules;
import software.ulpgc.chess.application.domain.service.MoveApplier;
import software.ulpgc.chess.application.domain.service.CastlingMoveGenerator;
import software.ulpgc.chess.application.domain.service.MoveGenerate;
import software.ulpgc.chess.application.domain.service.PawnMoveGenerate;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenPositionDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenBoardSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenPositionSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenStateSerializer;

import static org.assertj.core.api.Assertions.assertThat;

class MoveApplierTest {

    private static final String INITIAL = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private MoveApplier applier;
    private FenPositionDeserializer deserializer;
    private FenPositionSerializer serializer;

    @BeforeEach
    void setUp() {
        applier = new MoveApplier(new GameRules(new MoveGenerate(new PawnMoveGenerate()), new CastlingMoveGenerator()));
        deserializer = new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer());
        serializer = new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer());
    }

    private String apply(String fen, String from, String to) {
        Position result = applier.applyMove(deserializer.deserialize(fen), new Move(Square.at(from), Square.at(to)));
        return serializer.serialize(result);
    }

    // ---------- Turno y número de jugada ----------

    @Test
    @DisplayName("Un movimiento de las blancas pasa el turno a las negras y no sube el número de jugada")
    void whiteMoveChangesTurnAndKeepsFullMoveNumber() {
        assertThat(apply(INITIAL, "e2", "e4"))
                .isEqualTo("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
    }

    @Test
    @DisplayName("Un movimiento de las negras pasa el turno a las blancas y sube el número de jugada")
    void blackMoveChangesTurnAndIncrementsFullMoveNumber() {
        assertThat(apply("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1", "e7", "e5"))
                .isEqualTo("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2");
    }

    @Test
    @DisplayName("Aplicar un movimiento no modifica la posición original")
    void applyingAMoveDoesNotModifyTheOriginalPosition() {
        Position original = deserializer.deserialize(INITIAL);
        applier.applyMove(original, new Move(Square.at("e2"), Square.at("e4")));
        assertThat(serializer.serialize(original)).isEqualTo(INITIAL);
    }

    // ---------- Reloj de medios movimientos ----------

    @Test
    @DisplayName("Un movimiento de pieza sin captura suma uno al reloj de medios movimientos")
    void quietPieceMoveIncrementsHalfMoveClock() {
        assertThat(apply(INITIAL, "g1", "f3"))
                .isEqualTo("rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1");
    }

    @Test
    @DisplayName("Un movimiento de peón reinicia el reloj de medios movimientos")
    void pawnMoveResetsHalfMoveClock() {
        assertThat(apply("4k3/8/8/8/8/8/4P3/4K3 w - - 5 10", "e2", "e3"))
                .isEqualTo("4k3/8/8/8/8/4P3/8/4K3 b - - 0 10");
    }

    @Test
    @DisplayName("Una captura reinicia el reloj de medios movimientos")
    void captureResetsHalfMoveClock() {
        assertThat(apply("4k3/8/8/3p4/8/8/8/3RK3 w - - 7 20", "d1", "d5"))
                .isEqualTo("4k3/8/8/3R4/8/8/8/4K3 b - - 0 20");
    }

    // ---------- Derechos de enroque ----------

    @Test
    @DisplayName("Si se mueve el rey blanco, las blancas pierden sus dos derechos de enroque")
    void whiteKingMoveRemovesWhiteRights() {
        assertThat(apply("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "e1", "e2"))
                .isEqualTo("r3k2r/8/8/8/8/8/4K3/R6R b kq - 1 1");
    }

    @Test
    @DisplayName("Si se mueve el rey negro, las negras pierden sus dos derechos de enroque")
    void blackKingMoveRemovesBlackRights() {
        assertThat(apply("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1", "e8", "e7"))
                .isEqualTo("r6r/4k3/8/8/8/8/8/R3K2R w KQ - 1 2");
    }

    @Test
    @DisplayName("Si se mueve una torre desde su esquina, se pierde solo el derecho de ese lado")
    void rookMoveFromCornerRemovesThatRight() {
        assertThat(apply("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "a1", "a2"))
                .isEqualTo("r3k2r/8/8/8/8/8/R7/4K2R b Kkq - 1 1");
    }

    @Test
    @DisplayName("Si capturan una torre en su esquina, su dueño pierde ese derecho")
    void capturingARookInItsCornerRemovesTheRight() {
        assertThat(apply("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "a1", "a8"))
                .isEqualTo("R3k2r/8/8/8/8/8/8/4K2R b Kk - 0 1");
    }

    @Test
    @DisplayName("Un movimiento de otra pieza no cambia los derechos de enroque")
    void otherPieceMoveKeepsRights() {
        assertThat(apply("r3k2r/8/8/8/8/2N5/8/R3K2R w KQkq - 0 1", "c3", "d5"))
                .isEqualTo("r3k2r/8/8/3N4/8/8/8/R3K2R b KQkq - 1 1");
    }

    @Test
    @DisplayName("Mover el rey sin derechos de enroque no falla y no los inventa")
    void kingMoveWithoutRightsWorks() {
        assertThat(apply("4k3/8/8/8/8/8/8/4K3 w - - 0 1", "e1", "e2"))
                .isEqualTo("4k3/8/8/8/8/8/4K3/8 b - - 1 1");
    }

    // ---------- Enroque ----------

    @Test
    @DisplayName("El enroque corto de las blancas mueve el rey a g1 y la torre a f1")
    void whiteShortCastlingMovesTheRook() {
        assertThat(apply("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "e1", "g1"))
                .isEqualTo("r3k2r/8/8/8/8/8/8/R4RK1 b kq - 1 1");
    }

    @Test
    @DisplayName("El enroque largo de las blancas mueve el rey a c1 y la torre a d1")
    void whiteLongCastlingMovesTheRook() {
        assertThat(apply("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "e1", "c1"))
                .isEqualTo("r3k2r/8/8/8/8/8/8/2KR3R b kq - 1 1");
    }

    @Test
    @DisplayName("El enroque corto de las negras mueve el rey a g8 y la torre a f8")
    void blackShortCastlingMovesTheRook() {
        assertThat(apply("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1", "e8", "g8"))
                .isEqualTo("r4rk1/8/8/8/8/8/8/R3K2R w KQ - 1 2");
    }

    @Test
    @DisplayName("El enroque largo de las negras mueve el rey a c8 y la torre a d8")
    void blackLongCastlingMovesTheRook() {
        assertThat(apply("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1", "e8", "c8"))
                .isEqualTo("2kr3r/8/8/8/8/8/8/R3K2R w KQ - 1 2");
    }

    // ---------- Promoción ----------

    private String applyPromoting(String fen, String from, String to, Piece piece) {
        Position result = applier.applyMove(deserializer.deserialize(fen),
                new Move(Square.at(from), Square.at(to)), Optional.of(piece));
        return serializer.serialize(result);
    }

    @Test
    @DisplayName("Un peón blanco que llega a la última fila se convierte en la pieza elegida")
    void whitePawnPromotesToQueen() {
        assertThat(applyPromoting("7k/4P3/8/8/8/8/8/4K3 w - - 3 5", "e7", "e8", Piece.WhiteQueen))
                .isEqualTo("4Q2k/8/8/8/8/8/8/4K3 b - - 0 5");
    }

    @Test
    @DisplayName("Se puede promocionar a una pieza menor, por ejemplo un caballo")
    void whitePawnPromotesToKnight() {
        assertThat(applyPromoting("7k/4P3/8/8/8/8/8/4K3 w - - 3 5", "e7", "e8", Piece.WhiteKnight))
                .isEqualTo("4N2k/8/8/8/8/8/8/4K3 b - - 0 5");
    }

    @Test
    @DisplayName("Un peón negro que llega a la primera fila se convierte en la pieza elegida")
    void blackPawnPromotesToRook() {
        assertThat(applyPromoting("4k3/8/8/8/8/8/4p3/K7 b - - 0 1", "e2", "e1", Piece.BlackRook))
                .isEqualTo("4k3/8/8/8/8/8/8/K3r3 w - - 0 2");
    }

    @Test
    @DisplayName("Se puede promocionar capturando una pieza")
    void promotionWithCapture() {
        assertThat(applyPromoting("2r4k/3P4/8/8/8/8/8/4K3 w - - 0 1", "d7", "c8", Piece.WhiteQueen))
                .isEqualTo("2Q4k/8/8/8/8/8/8/4K3 b - - 0 1");
    }

    @Test
    @DisplayName("Promocionar capturando una torre en su esquina le quita el derecho de enroque")
    void promotionCapturingCornerRookRemovesRight() {
        assertThat(applyPromoting("r3k3/1P6/8/8/8/8/8/4K3 w q - 0 1", "b7", "a8", Piece.WhiteQueen))
                .isEqualTo("Q3k3/8/8/8/8/8/8/4K3 b - - 0 1");
    }

    @Test
    @DisplayName("Si el movimiento no es una promoción, la pieza indicada se ignora")
    void pieceIsIgnoredWhenThereIsNoPromotion() {
        assertThat(applyPromoting("7k/8/4P3/8/8/8/8/4K3 w - - 0 1", "e6", "e7", Piece.WhiteQueen))
                .isEqualTo("7k/4P3/8/8/8/8/8/4K3 b - - 0 1");
    }
}
