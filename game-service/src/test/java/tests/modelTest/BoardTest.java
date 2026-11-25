package tests.modelTest;

import software.ulpgc.chess.application.model.board.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.model.board.Board;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenBoardSerializer;

import static org.assertj.core.api.Assertions.assertThat;

class BoardTest {

    private static final String BOTH_SIDES = "r3k2r/8/8/8/8/8/8/R3K2R";

    private FenBoardDeserializer deserializer;
    private FenBoardSerializer serializer;

    @BeforeEach
    void setUp() {
        deserializer = new FenBoardDeserializer();
        serializer = new FenBoardSerializer();
    }

    private String move(String fen, String from, String to) {
        Board board = deserializer.deserialize(fen);
        return serializer.serialize(board.move(Square.at(from), Square.at(to)));
    }

    @Test
    @DisplayName("Un movimiento normal mueve la pieza y deja vacía la casilla de origen")
    void normalMove() {
        assertThat(move("4k3/8/8/8/8/8/4P3/4K3", "e2", "e4")).isEqualTo("4k3/8/8/8/4P3/8/8/4K3");
    }

    @Test
    @DisplayName("Una captura sustituye la pieza de la casilla de destino")
    void capture() {
        assertThat(move("4k3/8/8/3p4/8/8/8/3RK3", "d1", "d5")).isEqualTo("4k3/8/8/3R4/8/8/8/4K3");
    }

    @Test
    @DisplayName("El enroque corto blanco mueve también la torre de h1 a f1")
    void whiteShortCastling() {
        assertThat(move(BOTH_SIDES, "e1", "g1")).isEqualTo("r3k2r/8/8/8/8/8/8/R4RK1");
    }

    @Test
    @DisplayName("El enroque largo blanco mueve también la torre de a1 a d1")
    void whiteLongCastling() {
        assertThat(move(BOTH_SIDES, "e1", "c1")).isEqualTo("r3k2r/8/8/8/8/8/8/2KR3R");
    }

    @Test
    @DisplayName("El enroque corto negro mueve también la torre de h8 a f8")
    void blackShortCastling() {
        assertThat(move(BOTH_SIDES, "e8", "g8")).isEqualTo("r4rk1/8/8/8/8/8/8/R3K2R");
    }

    @Test
    @DisplayName("El enroque largo negro mueve también la torre de a8 a d8")
    void blackLongCastling() {
        assertThat(move(BOTH_SIDES, "e8", "c8")).isEqualTo("2kr3r/8/8/8/8/8/8/R3K2R");
    }

    @Test
    @DisplayName("Un rey que se mueve una casilla no mueve ninguna torre")
    void kingMovingOneSquareDoesNotMoveRooks() {
        assertThat(move(BOTH_SIDES, "e1", "f1")).isEqualTo("r3k2r/8/8/8/8/8/8/R4K1R");
    }

    @Test
    @DisplayName("Mover no modifica el tablero original")
    void moveDoesNotModifyTheOriginalBoard() {
        Board original = deserializer.deserialize(BOTH_SIDES);
        original.move(Square.at("e1"), Square.at("g1"));
        assertThat(serializer.serialize(original)).isEqualTo(BOTH_SIDES);
    }

    // ---------- Promoción ----------

    private String movePromoting(String fen, String from, String to, Piece piece) {
        Board board = deserializer.deserialize(fen);
        Board moved = board.move(Square.at(from), Square.at(to));
        return serializer.serialize(moved.promote(Square.at(to), piece));
    }

    @Test
    @DisplayName("Promote sustituye el peón blanco de la última fila por la pieza elegida")
    void promoteReplacesWhitePawn() {
        assertThat(movePromoting("7k/4P3/8/8/8/8/8/4K3", "e7", "e8", Piece.WhiteQueen))
                .isEqualTo("4Q2k/8/8/8/8/8/8/4K3");
    }

    @Test
    @DisplayName("Promote sustituye el peón negro de la primera fila por la pieza elegida")
    void promoteReplacesBlackPawn() {
        assertThat(movePromoting("4k3/8/8/8/8/8/4p3/K7", "e2", "e1", Piece.BlackKnight))
                .isEqualTo("4k3/8/8/8/8/8/8/K3n3");
    }

    @Test
    @DisplayName("Promote no modifica el tablero sobre el que se llama")
    void promoteDoesNotModifyTheOriginalBoard() {
        Board moved = deserializer.deserialize("7k/4P3/8/8/8/8/8/4K3").move(Square.at("e7"), Square.at("e8"));
        moved.promote(Square.at("e8"), Piece.WhiteRook);
        assertThat(serializer.serialize(moved)).isEqualTo("4P2k/8/8/8/8/8/8/4K3");
    }
}
