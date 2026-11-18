package tests.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.model.Board;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.Move;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.application.domain.service.MoveGenerate;
import software.ulpgc.chess.application.domain.service.PawnMoveGenerate;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoveGenerateTest {

    private static final String INITIAL = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

    private MoveGenerate generator;
    private FenBoardDeserializer deserializer;

    @BeforeEach
    void setUp() {
        generator = new MoveGenerate(new PawnMoveGenerate());
        deserializer = new FenBoardDeserializer();
    }

    private Move move(String from, String to) {
        return new Move(Square.at(from), Square.at(to));
    }

    private List<Move> movesOf(String fen, Color color) {
        Board board = deserializer.deserialize(fen);
        return generator.movesOf(board, color);
    }

    private List<Move> movesFrom(String fen, String square) {
        Board board = deserializer.deserialize(fen);
        Square source = Square.at(square);
        Color color = board.pieces().get(source).color();
        return generator.movesOf(board, color).stream()
                .filter(move -> move.from().equals(source))
                .toList();
    }

    // ---------- Caballo ----------

    @Test
    @DisplayName("El caballo en el centro tiene ocho destinos")
    void knightInTheCenter() {
        assertThat(movesFrom("8/8/8/8/4N3/8/8/8", "e4")).containsExactlyInAnyOrder(
                move("e4", "c3"), move("e4", "c5"), move("e4", "d2"), move("e4", "d6"),
                move("e4", "f2"), move("e4", "f6"), move("e4", "g3"), move("e4", "g5"));
    }

    @Test
    @DisplayName("El caballo en la esquina solo tiene dos destinos")
    void knightInTheCorner() {
        assertThat(movesFrom("8/8/8/8/8/8/8/N7", "a1")).containsExactlyInAnyOrder(
                move("a1", "b3"), move("a1", "c2"));
    }

    @Test
    @DisplayName("El caballo salta sobre las piezas, no puede caer en una propia y captura una rival")
    void knightJumpsOverAndCaptures() {
        // pieza propia en f6, rival en g5, piezas de por medio en e5 y d4
        List<Move> moves = movesFrom("8/8/5P2/4P1p1/3PN3/8/8/8", "e4");

        assertThat(moves).doesNotContain(move("e4", "f6"));
        assertThat(moves).contains(move("e4", "g5"));
        assertThat(moves).containsExactlyInAnyOrder(
                move("e4", "c3"), move("e4", "c5"), move("e4", "d2"), move("e4", "d6"),
                move("e4", "f2"), move("e4", "g3"), move("e4", "g5"));
    }

    // ---------- Rey ----------

    @Test
    @DisplayName("El rey en el centro tiene ocho destinos")
    void kingInTheCenter() {
        assertThat(movesFrom("8/8/8/8/4K3/8/8/8", "e4")).containsExactlyInAnyOrder(
                move("e4", "d3"), move("e4", "d4"), move("e4", "d5"), move("e4", "e3"),
                move("e4", "e5"), move("e4", "f3"), move("e4", "f4"), move("e4", "f5"));
    }

    @Test
    @DisplayName("El rey en la esquina solo tiene tres destinos")
    void kingInTheCorner() {
        assertThat(movesFrom("8/8/8/8/8/8/8/K7", "a1")).containsExactlyInAnyOrder(
                move("a1", "a2"), move("a1", "b1"), move("a1", "b2"));
    }

    @Test
    @DisplayName("El rey no puede ir a una casilla con pieza propia pero sí capturar una rival")
    void kingOwnAndRivalPieces() {
        List<Move> moves = movesFrom("8/8/8/8/8/8/Pp6/K7", "a1");

        assertThat(moves).containsExactlyInAnyOrder(move("a1", "b1"), move("a1", "b2"));
    }

    // ---------- Alfil, torre y dama ----------

    @Test
    @DisplayName("El alfil se detiene ante una pieza propia y captura una rival")
    void bishopBlockedAndCapturing() {
        // alfil en c1, peón propio en d2, peón rival en a3
        assertThat(movesFrom("8/8/8/8/8/p7/3P4/2B5", "c1")).containsExactlyInAnyOrder(
                move("c1", "b2"), move("c1", "a3"));
    }

    @Test
    @DisplayName("El alfil en un tablero vacío tiene trece destinos")
    void bishopOnEmptyBoard() {
        assertThat(movesFrom("8/8/8/8/4B3/8/8/8", "e4")).hasSize(13);
    }

    @Test
    @DisplayName("La torre no pasa de una pieza propia")
    void rookBlockedByOwnPiece() {
        List<Move> moves = movesFrom("8/8/8/8/8/8/8/R2P4", "a1");

        assertThat(moves).hasSize(9);
        assertThat(moves).contains(move("a1", "c1"));
        assertThat(moves).doesNotContain(move("a1", "d1"), move("a1", "e1"));
    }

    @Test
    @DisplayName("La torre captura una pieza rival y se detiene ahí")
    void rookCapturesAndStops() {
        List<Move> moves = movesFrom("8/8/8/8/8/8/8/R2p4", "a1");

        assertThat(moves).hasSize(10);
        assertThat(moves).contains(move("a1", "d1"));
        assertThat(moves).doesNotContain(move("a1", "e1"));
    }

    @Test
    @DisplayName("La dama en un tablero vacío tiene veintisiete destinos")
    void queenOnEmptyBoard() {
        assertThat(movesFrom("8/8/8/8/3Q4/8/8/8", "d4")).hasSize(27);
    }

    // ---------- Peón ----------

    @Test
    @DisplayName("El peón blanco en su fila inicial avanza una o dos casillas")
    void whitePawnFromStartRank() {
        assertThat(movesFrom("8/8/8/8/8/8/4P3/8", "e2")).containsExactlyInAnyOrder(
                move("e2", "e3"), move("e2", "e4"));
    }

    @Test
    @DisplayName("El peón negro en su fila inicial avanza hacia abajo")
    void blackPawnFromStartRank() {
        assertThat(movesFrom("8/4p3/8/8/8/8/8/8", "e7")).containsExactlyInAnyOrder(
                move("e7", "e6"), move("e7", "e5"));
    }

    @Test
    @DisplayName("El peón fuera de su fila inicial solo avanza una casilla")
    void pawnOutsideStartRank() {
        assertThat(movesFrom("8/8/8/8/4P3/8/8/8", "e4")).containsExactlyInAnyOrder(
                move("e4", "e5"));
    }

    @Test
    @DisplayName("El peón no avanza si tiene una pieza delante")
    void pawnBlockedInFront() {
        assertThat(movesFrom("8/8/8/8/8/4p3/4P3/8", "e2")).isEmpty();
    }

    @Test
    @DisplayName("El peón no salta una pieza para avanzar dos casillas")
    void pawnCannotJumpOverAPiece() {
        assertThat(movesFrom("8/8/8/8/4p3/8/4P3/8", "e2")).containsExactlyInAnyOrder(
                move("e2", "e3"));
    }

    @Test
    @DisplayName("El peón captura en diagonal solo piezas rivales")
    void pawnCapturesOnlyRivals() {
        // rival en d3, pieza propia en f3
        assertThat(movesFrom("8/8/8/8/8/3p1P2/4P3/8", "e2")).containsExactlyInAnyOrder(
                move("e2", "e3"), move("e2", "e4"), move("e2", "d3"));
    }

    @Test
    @DisplayName("El peón en el borde del tablero no captura por el lado que no existe")
    void pawnOnTheEdge() {
        assertThat(movesFrom("8/8/8/8/8/1p6/P7/8", "a2")).containsExactlyInAnyOrder(
                move("a2", "a3"), move("a2", "a4"), move("a2", "b3"));
    }

    @Test
    @DisplayName("El peón en la última fila no tiene movimientos ni lanza errores")
    void pawnOnLastRank() {
        assertThat(movesFrom("4P3/8/8/8/8/8/8/8", "e8")).isEmpty();
    }

    // ---------- Conjunto ----------

    @Test
    @DisplayName("En la posición inicial las blancas tienen veinte movimientos")
    void initialPositionWhite() {
        assertThat(movesOf(INITIAL, Color.White)).hasSize(20);
    }

    @Test
    @DisplayName("En la posición inicial las negras tienen veinte movimientos")
    void initialPositionBlack() {
        assertThat(movesOf(INITIAL, Color.Black)).hasSize(20);
    }

    @Test
    @DisplayName("Solo se generan movimientos del color pedido")
    void onlyMovesOfTheRequestedColor() {
        assertThat(movesOf("8/8/8/8/4n3/8/8/8", Color.White)).isEmpty();
    }
}