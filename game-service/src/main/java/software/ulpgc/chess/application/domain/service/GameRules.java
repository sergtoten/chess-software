package software.ulpgc.chess.application.domain.service;

import software.ulpgc.chess.application.model.board.Board;
import software.ulpgc.chess.application.model.game.GameStatus;
import software.ulpgc.chess.application.model.position.Move;
import software.ulpgc.chess.application.model.position.State;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Piece;
import software.ulpgc.chess.application.model.board.Square;

import java.util.ArrayList;
import java.util.List;

public class GameRules {

    private final MoveGenerate moveGenerate;
    private final CastlingMoveGenerator castlingMoveGenerator;

    public GameRules(MoveGenerate moveGenerate, CastlingMoveGenerator castlingMoveGenerator) {
        this.moveGenerate = moveGenerate;
        this.castlingMoveGenerator = castlingMoveGenerator;
    }

    public GameStatus statusOf(Board board, Color turn, State state) {
        boolean hasMoves = !legalMoves(board, turn, state).isEmpty();
        boolean inCheck = !kingSafe(board, turn);
        if (!hasMoves) return inCheck ? GameStatus.Checkmate : GameStatus.Stalemate;
        return inCheck ? GameStatus.Check : GameStatus.Ongoing;
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
        return moveGenerate.movesOf(board, turn.oppositeColor())
                .stream()
                .noneMatch(move -> {
                    Piece target = board.pieceAt(move.to());
                    return target != null && target.isKing();
                });
    }

    private List<Move> castlingLegalMoves(Board board, Color turn, State state) {
        return castlingMoveGenerator.castlingMoves(board, turn, state)
                .stream()
                .filter(move -> isCastlingLegal(board, turn, move))
                .toList();
    }

    private boolean isCastlingLegal(Board board, Color turn, Move move) {
        return kingSafe(board, turn) && isPassingSquareSafe(board, turn, move);
    }

    private boolean isPassingSquareSafe(Board board, Color turn, Move move) {
        Square passing = passingSquare(move);
        return kingSafe(board.move(move.from(), passing), turn);
    }

    private Square passingSquare(Move move) {
        int file = (move.from().file().ordinal() + move.to().file().ordinal()) / 2;
        return Square.at(file, move.from().rank().ordinal());
    }
}