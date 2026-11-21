package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.ports.persistence.GameStore;

import java.util.Optional;

public class RetrieveGameUseCase implements UseCase {
    private final GameStore store;

    public RetrieveGameUseCase(GameStore store) {
        this.store = store;
    }

    @Override
    public Output execute(Input input) {
        Optional<String> id = GameIdReader.from(input);
        if (id.isEmpty()) return output(400, new byte[0]);
        String game = store.load(id.get());
        if (game == null) return output(404, new byte[0]);
        return output(200, game.getBytes());
    }

    private static Output output(int status, byte[] result) {
        return new Output() {
            @Override
            public int status() {
                return status;
            }

            @Override
            public byte[] result() {
                return result;
            }
        };
    }
}
