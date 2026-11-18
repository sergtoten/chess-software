package software.ulpgc.chess.application.domain.service;

import software.ulpgc.chess.application.model.Board;
import software.ulpgc.chess.application.model.CastlingRight;
import software.ulpgc.chess.application.model.Move;
import software.ulpgc.chess.application.model.State;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Piece;
import software.ulpgc.chess.application.model.board.Square;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CastlingMoveGenerator {

    private static final int KING_FILE = 4;

    private static final int SHORT_ROOK_FILE = 7;
    private static final int SHORT_TARGET_FILE = 6;
    private static final int[] SHORT_EMPTY_FILES = {5, 6};

    private static final int LONG_ROOK_FILE = 0;
    private static final int LONG_TARGET_FILE = 2;
    private static final int[] LONG_EMPTY_FILES = {1, 2, 3};

    public List<Move> castlingMoves(Board board, Color turn, State state) {
        int rank = turn == Color.White ? 0 : 7;
        Square kingSquare = Square.at(KING_FILE, rank);
        if (!isKingOf(board.pieceAt(kingSquare), turn)) return List.of();
        return castlingMovesFor(board, turn, state, rank, kingSquare);
    }

    private List<Move> castlingMovesFor(Board board, Color turn, State state, int rank, Square kingSquare) {
        List<Move> moves = new ArrayList<>();
        CastlingRight shortRight = turn == Color.White ? CastlingRight.WhiteShort : CastlingRight.BlackShort;
        CastlingRight longRight = turn == Color.White ? CastlingRight.WhiteLong : CastlingRight.BlackLong;
        if (canCastle(board, turn, rank, state.castlings(), shortRight, SHORT_ROOK_FILE, SHORT_EMPTY_FILES)) {
            moves.add(new Move(kingSquare, Square.at(SHORT_TARGET_FILE, rank)));
        }
        if (canCastle(board, turn, rank, state.castlings(), longRight, LONG_ROOK_FILE, LONG_EMPTY_FILES)) {
            moves.add(new Move(kingSquare, Square.at(LONG_TARGET_FILE, rank)));
        }
        return moves;
    }

    private boolean canCastle(Board board, Color turn, int rank, Set<CastlingRight> castlings,
                              CastlingRight right, int rookFile, int[] emptyFiles) {
        if (!castlings.contains(right)) return false;
        if (!isRookOf(board.pieceAt(Square.at(rookFile, rank)), turn)) return false;
        for (int file : emptyFiles) {
            if (board.pieceAt(Square.at(file, rank)) != null) return false;
        }
        return true;
    }

    private boolean isKingOf(Piece piece, Color color) {
        return piece != null && piece.isKing() && piece.color() == color;
    }

    private boolean isRookOf(Piece piece, Color color) {
        return piece != null && piece.isRook() && piece.color() == color;
    }
}