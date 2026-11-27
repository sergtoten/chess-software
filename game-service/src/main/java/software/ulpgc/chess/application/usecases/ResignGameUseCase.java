package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.model.game.Game;
import software.ulpgc.chess.application.model.game.GameStatus;
import software.ulpgc.chess.application.model.game.Player;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.ports.Clock;
import software.ulpgc.chess.application.ports.deserializer.GameDeserializer;
import software.ulpgc.chess.application.ports.persistence.GameStore;
import software.ulpgc.chess.application.ports.serializer.GameSerializer;

import java.util.Optional;

public class ResignGameUseCase implements UseCase {

    private final GameStore store;
    private final Clock clock;
    private final GameDeserializer deserializer;
    private final GameSerializer serializer;
    public ResignGameUseCase(GameStore store, Clock clock, GameDeserializer deserializer, GameSerializer serializer) {
        this.store = store;
        this.clock = clock;
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    @Override
    public Output execute(Input input) {
        Optional<String> id = GameIdReader.from(input);
        if (id.isEmpty()) return UseCaseOutput.error(400, "missing or malformed id");
        Optional<Player> player = PlayerReader.from(input);
        if (player.isEmpty()) return UseCaseOutput.error(400, "missing or malformed playerId or playerName");
        Optional<String> stored = store.load(id.get());
        if (stored.isEmpty()) return UseCaseOutput.error(404, "game not found");
        Game game = deserializer.deserialize(stored.get());
        if (!isInProgress(game.status())) return UseCaseOutput.error(409, "game is not in progress");
        Optional<Color> loser = game.colorOf(player.get().id());
        if (loser.isEmpty()) return UseCaseOutput.error(403, "not a player of this game");
        return GameSaver.saveAndRespond(store, serializer, game.resign(loser.get(), clock.now()));
    }

    private static boolean isInProgress(GameStatus status) {
        return status == GameStatus.Ongoing || status == GameStatus.Check;
    }
}
