package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.ports.persistence.GameStore;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class RetrieveGameUseCase implements UseCase {
    private final GameStore store;

    public RetrieveGameUseCase(GameStore store) {
        this.store = store;
    }

    @Override
    public Output execute(Input input) {
        Optional<String> id = GameIdReader.from(input);
        if (id.isEmpty()) return UseCaseOutput.error(400, "missing or malformed id");
        Optional<String> game = store.load(id.get());
        if (game.isEmpty()) return UseCaseOutput.error(404, "game not found");
        return new UseCaseOutput(200, game.get().getBytes(StandardCharsets.UTF_8));
    }
}
