package software.ulpgc.chess.application.domain.service;

import software.ulpgc.chess.application.domain.model.*;

import java.util.ArrayList;
import java.util.List;

public class PawnMoveGenerate {

    private static final int[] PAWN_CAPTURE_FILES = {1, -1};
    public List<Move> pawnMoves(Board board, Square source, Piece pawn) {
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
            if (!Square.isInside(file, rank)) break;
            Square target = Square.at(file, rank);
            if (board.pieceAt(target) != null) break;
            moves.add(new Move(source, target));
        }
        return moves;
    }

    private List<Move> pawnCaptures(Board board, Square source, Piece pawn, int direction) {
        List<Move> moves = new ArrayList<>();
        int rank = source.rank().ordinal() + direction;
        for (int side : PAWN_CAPTURE_FILES) {
            int file = source.file().ordinal() + side;
            if (!Square.isInside(file, rank)) continue;
            Square target = Square.at(file, rank);
            Piece found = board.pieceAt(target);
            if (found != null && found.color() != pawn.color()) moves.add(new Move(source, target));
        }
        return moves;
    }


}
