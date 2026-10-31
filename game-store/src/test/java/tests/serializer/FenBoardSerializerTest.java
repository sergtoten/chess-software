package tests.serializer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenBoardSerializer;
import software.ulpgc.chess.application.model.Board;
import software.ulpgc.chess.application.model.Piece;
import software.ulpgc.chess.application.model.Square;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class FenBoardSerializerTest {

    private FenBoardSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new FenBoardSerializer();
    }

    @Test
    @DisplayName("Debe serializar correctamente la posición inicial de ajedrez")
    void shouldSerializeInitialBoard() {
        Board board = Board.initial();

        String fen = serializer.serialize(board);
        System.out.println(fen);
        assertThat(fen)
                .isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
    }

    @Test
    @DisplayName("Debe serializar un tablero vacío")
    void shouldSerializeEmptyBoard() {
        Board board = new Board(new HashMap<>());

        String fen = serializer.serialize(board);
        System.out.println(fen);
        assertThat(fen).isEqualTo("8/8/8/8/8/8/8/8");
    }

    @Test
    @DisplayName("Debe serializar una posición con solo los dos reyes")
    void shouldSerializeCustomPosition() {
        Board board = new Board(new HashMap<>());
        board.pieces().put(Square.at("e1"), Piece.WhiteKing);
        board.pieces().put(Square.at("e8"), Piece.BlackKing);

        String fen = serializer.serialize(board);
        System.out.println(fen);
        assertThat(fen).isEqualTo("4k3/8/8/8/8/8/8/4K3");
    }

    @Test
    @DisplayName("Debe representar correctamente las casillas vacías entre piezas")
    void shouldSerializeEmptySquaresBetweenPieces() {
        Board board = new Board(new HashMap<>());
        board.pieces().put(Square.at("a8"), Piece.BlackRook);
        board.pieces().put(Square.at("d8"), Piece.BlackKnight);
        board.pieces().put(Square.at("h8"), Piece.BlackKing);

        String fen = serializer.serialize(board);
        System.out.println(fen);
        assertThat(fen).isEqualTo("r2n3k/8/8/8/8/8/8/8");
    }

    @Test
    @DisplayName("Debe serializar correctamente las piezas blancas y negras")
    void shouldSerializeWhiteAndBlackPieces() {
        Board board = new Board(new HashMap<>());
        board.pieces().put(Square.at("a1"), Piece.WhiteRook);
        board.pieces().put(Square.at("b1"), Piece.WhiteKnight);
        board.pieces().put(Square.at("c1"), Piece.WhiteBishop);
        board.pieces().put(Square.at("d1"), Piece.WhiteQueen);
        board.pieces().put(Square.at("e1"), Piece.WhiteKing);
        board.pieces().put(Square.at("a8"), Piece.BlackRook);
        board.pieces().put(Square.at("b8"), Piece.BlackKnight);
        board.pieces().put(Square.at("c8"), Piece.BlackBishop);
        board.pieces().put(Square.at("d8"), Piece.BlackQueen);
        board.pieces().put(Square.at("e8"), Piece.BlackKing);

        String fen = serializer.serialize(board);
        System.out.println(fen);

        assertThat(fen).isEqualTo(
                "rnbqk3/8/8/8/8/8/8/RNBQK3"
        );

    }

    @Test
    @DisplayName("Debe separar las ocho filas con barras y sin barras adicionales")
    void shouldSeparateRanksWithSlashes() {
        Board board = new Board(new HashMap<>());

        String fen = serializer.serialize(board);
        System.out.println(fen);
        assertThat(fen)
                .contains("/")
                .doesNotStartWith("/")
                .doesNotEndWith("/")
                .hasSize(15);
    }
}