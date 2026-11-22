package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.model.Game;
import software.ulpgc.chess.application.model.GameStatus;
import software.ulpgc.chess.application.model.Player;
import software.ulpgc.chess.application.ports.Clock;
import software.ulpgc.chess.application.ports.deserializer.GameDeserializer;
import software.ulpgc.chess.application.ports.persistence.GameStore;
import software.ulpgc.chess.application.ports.persistence.RoomIndex;
import software.ulpgc.chess.application.ports.serializer.GameSerializer;

import java.util.Optional;

public class JoinGameUseCase implements UseCase {

    private final GameStore store;
    private final Clock clock;
    private final RoomIndex index;
    private final GameDeserializer deserializer;
    private final GameSerializer serializer;

    public JoinGameUseCase(GameStore store, Clock clock, RoomIndex index, GameDeserializer deserializer, GameSerializer serializer) {
        this.store = store;
        this.clock = clock;
        this.index = index;
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    @Override
    public Output execute(Input input) {
        Optional<Player> guest = PlayerReader.from(input);
        if (guest.isEmpty()) return UseCaseOutput.error(400, "missing playerId or playerName");
        Optional<String> roomCode = RoomCodeReader.from(input);
        if (roomCode.isEmpty()) return UseCaseOutput.error(400, "missing or malformed roomCode");
        Optional<String> id = index.findRoom(roomCode.get());
        if (id.isEmpty()) return UseCaseOutput.error(404, "room not found");
        Optional<String> stored = store.load(id.get());
        if (stored.isEmpty()) return UseCaseOutput.error(404, "game not found");
        Game game = deserializer.deserialize(stored.get());
        if (game.status() != GameStatus.Waiting) return UseCaseOutput.error(409, "game is not waiting for a player");
        if (isPlayerOf(game, guest.get())) return UseCaseOutput.error(409, "player is already in this game");
        Game joined = game.join(guest.get(), clock.now());
        String json = serializer.serialize(joined);
        store.save(joined.id(), json);
        return new UseCaseOutput(200, json.getBytes());
    }

    private static boolean isPlayerOf(Game game, Player player) {
        return sameId(game.white(), player) || sameId(game.black(), player);
    }

    private static boolean sameId(Optional<Player> seat, Player player) {
        return seat.map(p -> p.uuid().equals(player.uuid())).orElse(false);
    }
}
