package software.ulpgc.chess.application.domain.service;

import software.ulpgc.chess.application.domain.model.Board;
import software.ulpgc.chess.application.domain.model.Color;
import software.ulpgc.chess.application.domain.model.Move;
import software.ulpgc.chess.application.domain.model.Piece;

import java.util.ArrayList;
import java.util.List;

public class GameRules {

    private final MoveGenerate moveGenerate;

    public GameRules(MoveGenerate moveGenerate) {
        this.moveGenerate = moveGenerate;
    }

    private boolean kingSafe(Board board, Color turn) {
        Color color = turn.oppositeColor();
        List<Move> moves = moveGenerate.movesOf(board, color);
        for (Move move : moves) {
            Piece to = board.pieces().get(move.to());
            if (to != null && to.isKing()) {
                return false;
            }
        }
        return true;
    }

    public List<Move> legalMove(Board board, Color turn) {
        List<Move> legalMoves = new ArrayList<>();
        List<Move> moves = moveGenerate.movesOf(board, turn);
        for (Move move : moves) {
            Board resultBoard = board.move(move.from(), move.to());
            if (kingSafe(resultBoard, turn)) legalMoves.add(move);
        }
        return legalMoves;
    }

    public boolean isStalemate(Board board, Color turn) {
        List<Move> legalMoves = legalMove(board, turn);
        return legalMoves.isEmpty() && kingSafe(board, turn);
    }

    public boolean isCheckmate(Board board, Color turn) {
        List<Move> legalMoves = legalMove(board, turn);
        return legalMoves.isEmpty() && !kingSafe(board, turn);
    }

}
