package software.ulpgc.chess.application.domain.service;

import software.ulpgc.chess.application.domain.model.*;

import java.util.*;

public class MoveGenerate {


    public List<Move> movesOf(Board actualBoard, Color color) {
        List<Move> moves = new ArrayList<>();
        for  (Map.Entry<Square, Piece> entry : actualBoard.pieces().entrySet()) {
            Piece piece = entry.getValue();
            Square source = entry.getKey();
            if (piece.color() !=  color) continue;
            moves.addAll(movesFrom(actualBoard, source));
        }
        return moves;
    }

    private static final int[][] BISHOP = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] ROOK   = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] KNIGHT = {
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2},
            {2, 1}, {2, -1}, {-2, -1}, {-2, -1}
    };

    private static final int[][] KING = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    private List<Move> movesFrom(Board board, Square source) {
        Piece piece = board.pieces().get(source);
        List<Move> moves = new ArrayList<>();
        if (piece.isBishop() || piece.isQueen()) moves.addAll(walk(board, source, BISHOP, true));
        if (piece.isRook() || piece.isQueen())   moves.addAll(walk(board, source, ROOK, true));
        if (piece.isKnight())   moves.addAll(walk(board, source, KNIGHT, false));
        if (piece.isKing())   moves.addAll(walk(board, source, KING, false));
        return moves;
    }

    private List<Move> walk(Board board, Square source, int[][] directions, boolean moveType) {
        Color color = board.pieces().get(source).color();
        List<Move> moves = new ArrayList<>();
        for (int[] direction : directions) {
            int file = source.file().ordinal() + direction[0];
            int rank = source.rank().ordinal() + direction[1];
            if (moveType) {
                moves.addAll(slide(board, file, rank, direction, color, source));
            } else {
                moves.addAll(jump(board, file, rank, color, source));
            }
        }
        return moves;
    }

    private Collection<Move> jump(Board board, int file, int rank, Color color, Square source) {
        List<Move> moves = new ArrayList<>();
        Square target = Square.at(file, rank);
        Piece found = board.pieces().get(target);
        if (found == null) {
            moves.add(new Move(source, target));
        } else if (found.color() != color) moves.add(new Move(source, target));

        return moves;
    }

    private List<Move> slide(Board board, int file, int rank, int[] direction, Color color, Square source) {
        List<Move> moves = new ArrayList<>();
        while (isInside(file, rank)) {
            Square target = Square.at(file, rank);
            Piece found = board.pieces().get(target);
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

    private boolean isInside(int file, int rank) {
        return (file >= 0 && file < 8) && (rank >= 0 && rank < 8);
    }

}
