package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.domain.IllegalMoveException;
import software.ulpgc.chess.application.domain.service.GameRules;
import software.ulpgc.chess.application.domain.service.MoveApplier;
import software.ulpgc.chess.application.model.Game;
import software.ulpgc.chess.application.model.GameStatus;
import software.ulpgc.chess.application.model.Player;
import software.ulpgc.chess.application.model.Position;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Piece;
import software.ulpgc.chess.application.ports.Clock;
import software.ulpgc.chess.application.ports.deserializer.GameDeserializer;
import software.ulpgc.chess.application.ports.persistence.GameStore;
import software.ulpgc.chess.application.ports.serializer.GameSerializer;
import software.ulpgc.chess.application.usecases.MoveReader.MoveRequest;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class UpdateGameUseCase implements UseCase {

    private final GameStore store;
    private final Clock clock;
    private final GameDeserializer deserializer;
    private final GameSerializer serializer;
    private final MoveApplier applier;
    private final GameRules rules;

    public UpdateGameUseCase(GameStore store, Clock clock, GameDeserializer deserializer, GameSerializer serializer, MoveApplier applier, GameRules rules) {
        this.store = store;
        this.clock = clock;
        this.deserializer = deserializer;
        this.serializer = serializer;
        this.applier = applier;
        this.rules = rules;
    }

    @Override
    public Output execute(Input input) {
        Optional<String> id = GameIdReader.from(input);
        if (id.isEmpty()) return UseCaseOutput.error(400, "missing or malformed id");
        Optional<Player> player = PlayerReader.from(input);
        if (player.isEmpty()) return UseCaseOutput.error(400, "missing playerId or playerName");
        Optional<MoveRequest> request = MoveReader.from(input);
        if (request.isEmpty()) return UseCaseOutput.error(400, "missing or malformed move or promotion");
        Optional<String> stored = store.load(id.get());
        if (stored.isEmpty()) return UseCaseOutput.error(404, "game not found");
        Game game = deserializer.deserialize(stored.get());
        if (!isInProgress(game.status())) return UseCaseOutput.error(409, "game is not in progress");
        Optional<Color> color = game.colorOf(player.get().id());
        if (color.isEmpty()) return UseCaseOutput.error(403, "not a player of this game");
        if (color.get() != game.position().turn()) return UseCaseOutput.error(409, "not your turn");
        Optional<Piece> promotion = request.get().promotionKind().map(kind -> Piece.valueOf(color.get().name() + kind));
        Position next;
        try {
            next = applier.applyMove(game.position(), request.get().move(), promotion);
        } catch (IllegalMoveException e) {
            return UseCaseOutput.error(422, "illegal move");
        }
        GameStatus status = rules.statusOf(next.board(), next.turn(), next.state());
        Game moved = game.withMove(request.get().move(), next, status, clock.now());
        String json = serializer.serialize(moved);
        store.save(moved.id(), json);
        return new UseCaseOutput(200, json.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isInProgress(GameStatus status) {
        return status == GameStatus.Ongoing || status == GameStatus.Check;
    }
}
