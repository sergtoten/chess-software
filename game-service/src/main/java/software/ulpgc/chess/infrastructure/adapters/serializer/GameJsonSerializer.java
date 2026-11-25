package software.ulpgc.chess.infrastructure.adapters.serializer;

import com.google.gson.Gson;
import software.ulpgc.chess.application.model.game.Game;
import software.ulpgc.chess.application.model.position.Move;
import software.ulpgc.chess.application.model.game.PlayedMove;
import software.ulpgc.chess.application.model.game.Player;
import software.ulpgc.chess.application.model.board.Piece;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.application.ports.serializer.GameSerializer;
import software.ulpgc.chess.application.ports.serializer.PositionSerializer;
import software.ulpgc.chess.infrastructure.adapters.dto.GameJson;
import software.ulpgc.chess.infrastructure.adapters.dto.PlayerJson;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameJsonSerializer implements GameSerializer {
    private final PositionSerializer positionSerializer;
    private final Gson gson = new Gson();

    private static final Map<Piece, String> PROMOTION_LETTERS = Map.of(
            Piece.WhiteQueen, "q",  Piece.BlackQueen, "q",
            Piece.WhiteRook, "r",   Piece.BlackRook, "r",
            Piece.WhiteBishop, "b", Piece.BlackBishop, "b",
            Piece.WhiteKnight, "n", Piece.BlackKnight, "n");

    public GameJsonSerializer(PositionSerializer positionSerializer) {
        this.positionSerializer = positionSerializer;
    }

    @Override
    public String serialize(Game game) {
        return gson.toJson(toJson(game));
    }

    private GameJson toJson(Game game) {
        return new GameJson(
                game.id(),
                game.roomCode(),
                player(game.white()),
                player(game.black()),
                positionSerializer.serialize(game.position()),
                moves(game.moves()),
                game.status().name(),
                game.result().map(Enum::name).orElse(null),
                game.createdAt().toString(),
                game.lastActivity().toString()
        );
    }

    private PlayerJson player(Optional<Player> player) {
        return player.map(p -> new PlayerJson(p.id(), p.name())).orElse(null);
    }

    private List<String> moves(List<PlayedMove> moves) {
        return moves.stream().map(this::move).toList();
    }

    private String move(PlayedMove played) {
        Move move = played.move();
        return square(move.from()) + square(move.to()) + played.promotion().map(this::promotionLetter).orElse("");
    }

    private String promotionLetter(Piece piece) {
        return PROMOTION_LETTERS.get(piece);
    }

    private String square(Square square) {
        return "" + square.file().symbol() + square.rank().symbol();
    }
}
