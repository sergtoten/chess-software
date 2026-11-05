package software.ulpgc.chess.application.domain.service;

import software.ulpgc.chess.application.domain.model.*;

import java.util.ArrayList;
import java.util.List;

public class GameRules {

    private final MoveGenerate moveGenerate;
    private final CastlingMoveGenerator castlingMoveGenerator;
    public GameRules(MoveGenerate moveGenerate, CastlingMoveGenerator castlingMoveGenerator) {
        this.moveGenerate = moveGenerate;
        this.castlingMoveGenerator = castlingMoveGenerator;
    }

    public boolean isCheckmate(Board board, Color turn, State state) {
        List<Move> legalMoves = legalMoves(board, turn, state);
        return legalMoves.isEmpty() && !kingSafe(board, turn);
    }

    public boolean isStalemate(Board board, Color turn, State state) {
        List<Move> legalMoves = legalMoves(board, turn, state);
        return legalMoves.isEmpty() && kingSafe(board, turn);
    }

    public List<Move> legalMoves(Board board, Color turn, State state) {
        List<Move> legalMoves = new ArrayList<>();
        List<Move> moves = new ArrayList<>(moveGenerate.movesOf(board, turn));
        moves.addAll(castlingLegalMoves(board, turn, state));
        for (Move move : moves) {
            Board resultBoard = board.move(move.from(), move.to());
            if (kingSafe(resultBoard, turn)) legalMoves.add(move);
        }
        return List.copyOf(legalMoves);
    }

    private boolean kingSafe(Board board, Color turn) {
        Color color = turn.oppositeColor();
        List<Move> moves = moveGenerate.movesOf(board, color);
        for (Move move : moves) {
            Piece to = board.pieceAt(move.to());
            if (to != null && to.isKing()) {
                return false;
            }
        }
        return true;
    }

    private List<Move> castlingLegalMoves(Board board, Color turn, State state) {
        return castlingMoveGenerator.castlingMoves(board, turn, state)
                .stream()
                .filter(move -> isCastlingLegal(board, turn, move))
                .toList();
    }

    private boolean isCastlingLegal(Board board, Color turn, Move move) {
        if (!kingSafe(board, turn)) return false;
        if (!passSquare(board, turn, move)) return false;
        if (!kingSafe(board.move(move.from(), move.to()), turn)) return false;
        return true;
    }

    private boolean passSquare(Board board, Color turn, Move move) {
        Square pass = passSquare(move);
        return kingSafe(board.move(move.from(), pass), turn);
    }

    private Square passSquare(Move move) {
        if (move.to().equals(Square.at("g1"))) return Square.at("f1");
        if (move.to().equals(Square.at("c1"))) return Square.at("d1");
        if (move.to().equals(Square.at("g8"))) return Square.at("f8");
        if (move.to().equals(Square.at("c8"))) return Square.at("d8");
        return null;
    }

}
