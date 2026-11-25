package software.ulpgc.chess.application.domain.service;

import software.ulpgc.chess.application.model.board.Board;
import software.ulpgc.chess.application.model.position.Move;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Piece;
import software.ulpgc.chess.application.model.board.Square;

import java.util.*;

public class MoveGenerate {

    private final PawnMoveGenerate pawnMoveGenerate;

    public MoveGenerate(PawnMoveGenerate pawnMoveGenerate) {
        this.pawnMoveGenerate = pawnMoveGenerate;
    }

    public List<Move> movesOf(Board actualBoard, Color color) {
        return actualBoard.pieces().entrySet().stream()
                .filter(entry -> entry.getValue().color() == color)
                .flatMap(entry -> movesFrom(actualBoard, entry.getKey()).stream())
                .toList();
    }

    private static final int[][] BISHOP = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] ROOK   = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] KNIGHT = {
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2},
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1}
    };

    private static final int[][] KING = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    private List<Move> movesFrom(Board board, Square source) {
        Piece piece = board.pieceAt(source);
        List<Move> moves = new ArrayList<>();
        if (piece.isBishop() || piece.isQueen()) moves.addAll(walk(board, source, BISHOP, true));
        if (piece.isRook() || piece.isQueen())   moves.addAll(walk(board, source, ROOK, true));
        if (piece.isKnight())   moves.addAll(walk(board, source, KNIGHT, false));
        if (piece.isKing())   moves.addAll(walk(board, source, KING, false));
        if (piece.isPawn()) moves.addAll(pawnMoveGenerate.pawnMoves(board, source, piece));
        return moves;
    }

    private List<Move> walk(Board board, Square source, int[][] directions, boolean sliding) {
        Color color = board.pieceAt(source).color();
        List<Move> moves = new ArrayList<>();
        for (int[] direction : directions) {
            int file = source.file().ordinal() + direction[0];
            int rank = source.rank().ordinal() + direction[1];
            if (sliding) {
                moves.addAll(slide(board, file, rank, direction, color, source));
            } else {
                moves.addAll(jump(board, file, rank, color, source));
            }
        }
        return moves;
    }

    private List<Move> jump(Board board, int file, int rank, Color color, Square source) {
        List<Move> moves = new ArrayList<>();
        if (!Square.isInside(file, rank)) {return moves;}
        Square target = Square.at(file, rank);
        Piece found = board.pieceAt(target);
        if (found == null || found.color() != color) moves.add(new Move(source, target));
        return moves;
    }

    private List<Move> slide(Board board, int file, int rank, int[] direction, Color color, Square source) {
        List<Move> moves = new ArrayList<>();
        while (Square.isInside(file, rank)) {
            Square target = Square.at(file, rank);
            Piece found = board.pieceAt(target);
            if (found == null) {
                moves.add(new Move(source, target));
            } else {
                if (found.color() != color) moves.add(new Move(source, target));
                break;
            }
            file += direction[0];
            rank += direction[1];
        }
        return moves;
    }
}
