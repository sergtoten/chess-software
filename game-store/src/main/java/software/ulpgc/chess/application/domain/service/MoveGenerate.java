package software.ulpgc.chess.application.domain.service;

import software.ulpgc.chess.application.domain.model.*;

import java.util.*;

public class MoveGenerate {


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
        Piece piece = board.pieces().get(source);
        List<Move> moves = new ArrayList<>();
        if (piece.isBishop() || piece.isQueen()) moves.addAll(walk(board, source, BISHOP, true));
        if (piece.isRook() || piece.isQueen())   moves.addAll(walk(board, source, ROOK, true));
        if (piece.isKnight())   moves.addAll(walk(board, source, KNIGHT, false));
        if (piece.isKing())   moves.addAll(walk(board, source, KING, false));
        if (piece.isPawn()) moves.addAll(pawnMoves(board, source, piece));
        return moves;
    }

    private static final int[] PAWN_CAPTURE_FILES = {1, -1};
    private List<Move> pawnMoves(Board board, Square source, Piece pawn) {
        int direction = pawn.color() == Color.White ? 1 : -1;
        Rank startRank = pawn.color() == Color.White ? Rank.R2 : Rank.R7;
        List<Move> moves = new ArrayList<>();
        moves.addAll(pawnAdvances(board, source, direction, startRank));
        moves.addAll(pawnCaptures(board, source, pawn, direction));
        return moves;
    }

    private List<Move> pawnAdvances(Board board, Square source, int direction, Rank startRank) {
        List<Move> moves = new ArrayList<>();
        int file = source.file().ordinal();
        int maxSteps = source.rank() == startRank ? 2 : 1;
        for (int step = 1; step <= maxSteps; step++) {
            int rank = source.rank().ordinal() + (step * direction) ;
            if (!isInside(file, rank)) break;
            Square target = Square.at(file, rank);
            if (board.pieces().get(target) != null) break;
            moves.add(new Move(source, target));
        }
        return moves;
    }

    private List<Move> pawnCaptures(Board board, Square source, Piece pawn, int direction) {
        List<Move> moves = new ArrayList<>();
        int rank = source.rank().ordinal() + direction;
        for (int side : PAWN_CAPTURE_FILES) {
            int file = source.file().ordinal() + side;
            if (!isInside(file, rank)) continue;
            Square target = Square.at(file, rank);
            Piece found = board.pieces().get(target);
            if (found != null && found.color() != pawn.color()) moves.add(new Move(source, target));
        }
        return moves;
    }

    private List<Move> walk(Board board, Square source, int[][] directions, boolean sliding) {
        Color color = board.pieces().get(source).color();
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
        if (!isInside(file, rank)) {return moves;}
        Square target = Square.at(file, rank);
        Piece found = board.pieces().get(target);
        if (found == null || found.color() != color) moves.add(new Move(source, target));
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
