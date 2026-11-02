package software.ulpgc.chess.application.domain.service;

import software.ulpgc.chess.application.domain.model.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MoveApplier {

    private final GameRules gameRules;

    public MoveApplier(GameRules gameRules) {
        this.gameRules = gameRules;
    }

    private Color playTurn(State state) {
        return state.turn();
    }
    public Position applyMove(Position position, Move move) {
        Color colorTurn = playTurn(position.state());
        Board actualBoard = position.board();
        List<Move> legalMoves = gameRules.legalMove(position.board(), colorTurn);
        if (!legalMoves.contains(move)) return null;
        State state = updateState(actualBoard, position, colorTurn, move);
        return new Position(position.board().move(move.from(), move.to()), state);

    }

    private State updateState(Board actualBoard, Position position, Color turn, Move move) {
        int fullMoveNumber = position.state().fullMoveNumber();
        if (turn == Color.Black) fullMoveNumber++;
        int halfMoveClock = updateHalfMove(actualBoard, position, turn, move);
        Set<CastlingRight> castling = castlingRight(actualBoard, move, turn, position);
        return new State(turn.oppositeColor(), castling, Optional.empty(), halfMoveClock, fullMoveNumber);
    }

    private Set<CastlingRight> castlingRight(Board actualBoard, Move move, Color turn, Position position) {
        Piece sourcePiece = actualBoard.pieces().get(move.from());
        Set<CastlingRight> castling = EnumSet.noneOf(CastlingRight.class);
        castling.addAll(position.state().castlings());

        if (sourcePiece.isKing()) {
            if (turn == Color.White) {
                castling.remove(CastlingRight.WhiteShort);
                castling.remove(CastlingRight.WhiteLong);
            } else {
                castling.remove(CastlingRight.BlackShort);
                castling.remove(CastlingRight.BlackLong);
            }
        }
        removeIfCorner(castling, move.from());
        removeIfCorner(castling, move.to());
        return castling;
    }

    private void removeIfCorner(Set<CastlingRight> castling, Square square) {
        if (square.equals(Square.at("a1"))) castling.remove(CastlingRight.WhiteLong);
        if (square.equals(Square.at("h1"))) castling.remove(CastlingRight.WhiteShort);
        if (square.equals(Square.at("a8"))) castling.remove(CastlingRight.BlackLong);
        if (square.equals(Square.at("h8"))) castling.remove(CastlingRight.BlackShort);
    }
    private int updateHalfMove(Board actualBoard, Position position, Color turn, Move move) {
        Piece sourcePiece = actualBoard.pieces().get(move.from());
        Piece destinationPiece = actualBoard.pieces().get(move.to());

        if (sourcePiece.isPawn()) return 0;
        else if (destinationPiece != null && destinationPiece.color() != turn) return 0;
        return position.state().halfMoveClock() + 1;
    }

}
