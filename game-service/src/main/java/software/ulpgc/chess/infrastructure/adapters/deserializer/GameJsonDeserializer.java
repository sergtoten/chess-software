package software.ulpgc.chess.infrastructure.adapters.deserializer;

import com.google.gson.Gson;
import software.ulpgc.chess.application.model.Game;
import software.ulpgc.chess.application.model.GameResult;
import software.ulpgc.chess.application.model.GameStatus;
import software.ulpgc.chess.application.model.Move;
import software.ulpgc.chess.application.model.Player;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.application.ports.deserializer.GameDeserializer;
import software.ulpgc.chess.application.ports.deserializer.PositionDeserializer;
import software.ulpgc.chess.infrastructure.adapters.dto.GameJson;
import software.ulpgc.chess.infrastructure.adapters.dto.PlayerJson;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class GameJsonDeserializer implements GameDeserializer {
    private final PositionDeserializer positionDeserializer;
    private final Gson gson = new Gson();

    public GameJsonDeserializer(PositionDeserializer positionDeserializer) {
        this.positionDeserializer = positionDeserializer;
    }

    @Override
    public Game deserialize(String data) {
        return toGame(gson.fromJson(data, GameJson.class));
    }

    private Game toGame(GameJson json) {
        return new Game(
                json.id(),
                json.roomCode(),
                player(json.white()),
                player(json.black()),
                positionDeserializer.deserialize(json.position()),
                moves(json.moves()),
                GameStatus.valueOf(json.status()),
                Optional.ofNullable(json.result()).map(GameResult::valueOf),
                Instant.parse(json.createdAt()),
                Instant.parse(json.lastActivity())
        );
    }

    private Optional<Player> player(PlayerJson player) {
        return Optional.ofNullable(player).map(p -> new Player(p.id(), p.name()));
    }

    private List<Move> moves(List<String> moves) {
        return moves.stream().map(this::move).toList();
    }

    private Move move(String move) {
        return new Move(Square.at(move.substring(0, 2)), Square.at(move.substring(2, 4)));
    }
}
