package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.domain.IllegalMoveException;
import software.ulpgc.chess.application.domain.service.GameRules;
import software.ulpgc.chess.application.domain.service.MoveApplier;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Piece;
import software.ulpgc.chess.application.model.game.Game;
import software.ulpgc.chess.application.model.game.GameStatus;
import software.ulpgc.chess.application.model.game.Player;
import software.ulpgc.chess.application.model.position.Move;
import software.ulpgc.chess.application.model.position.Position;
import software.ulpgc.chess.application.ports.Clock;
import software.ulpgc.chess.application.ports.deserializer.GameDeserializer;
import software.ulpgc.chess.application.ports.persistence.GameStore;
import software.ulpgc.chess.application.ports.serializer.GameSerializer;
import software.ulpgc.chess.application.usecases.MoveReader.MoveRequest;

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
        Optional<Game> moved = play(game, color.get(), request.get());
        if (moved.isEmpty()) return UseCaseOutput.error(422, "illegal move");
        return GameSaver.saveAndRespond(store, serializer, moved.get());
    }

    private Optional<Position> nextPosition(Position position, Move move, Optional<Piece> promotion) {
        try {
            return Optional.of(applier.applyMove(position, move, promotion));
        } catch (IllegalMoveException e) {
            return Optional.empty();
        }
    }

    private static boolean isInProgress(GameStatus status) {
        return status == GameStatus.Ongoing || status == GameStatus.Check;
    }

    private Optional<Game> play(Game game, Color color, MoveRequest request) {
        Optional<Piece> promotion = request.promotionKind().map(kind -> Piece.valueOf(color.name() + kind));
        Optional<Position> next = nextPosition(game.position(), request.move(), promotion);
        if (next.isEmpty()) return Optional.empty();
        GameStatus status = rules.statusOf(next.get().board(), next.get().turn(), next.get().state());
        return Optional.of(game.withMove(request.move(), next.get(), status, clock.now(), promotion));
    }
}
