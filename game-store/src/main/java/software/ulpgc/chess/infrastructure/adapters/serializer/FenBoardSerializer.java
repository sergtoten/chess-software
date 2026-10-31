package software.ulpgc.chess.infrastructure.adapters.serializer;

import software.ulpgc.chess.application.model.Board;
import software.ulpgc.chess.application.model.File;
import software.ulpgc.chess.application.model.Piece;
import software.ulpgc.chess.application.model.Rank;
import software.ulpgc.chess.application.model.Square;
import software.ulpgc.chess.application.ports.serializer.BoardSerializer;

import java.util.Map;

public class FenBoardSerializer implements BoardSerializer {

    private static final Map<Piece, Character> PIECES = Map.ofEntries(
            Map.entry(Piece.WhitePawn, 'P'),   Map.entry(Piece.BlackPawn, 'p'),
            Map.entry(Piece.WhiteKnight, 'N'), Map.entry(Piece.BlackKnight, 'n'),
            Map.entry(Piece.WhiteBishop, 'B'), Map.entry(Piece.BlackBishop, 'b'),
            Map.entry(Piece.WhiteRook, 'R'),   Map.entry(Piece.BlackRook, 'r'),
            Map.entry(Piece.WhiteQueen, 'Q'),  Map.entry(Piece.BlackQueen, 'q'),
            Map.entry(Piece.WhiteKing, 'K'),   Map.entry(Piece.BlackKing, 'k')
    );
    @Override
    public String serialize(Board board) {
        StringBuilder fen = new StringBuilder();
        for (int rank = Rank.values().length - 1; rank >= 0; rank--) {
            fen.append(serializeRank(board, rank));
            if (rank > 0 ) fen.append("/");
        }
        return fen.toString();
    }

    private String serializeRank(Board board, int rank) {
        StringBuilder row = new StringBuilder();
        int empty = 0;
        for (int file = 0; file < File.values().length; file++) {
            Piece piece = board.pieces().get(Square.at(file, rank));
            if (piece == null) {
                empty++;
            } else {
                if (empty > 0) {
                    row.append(empty);
                    empty = 0;
                }
                row.append(pieceFor(piece));
            }
        }
        if (empty > 0) row.append(empty);
        return row.toString();
    }

    private char pieceFor(Piece piece) {
        return PIECES.get(piece);
    }

}
