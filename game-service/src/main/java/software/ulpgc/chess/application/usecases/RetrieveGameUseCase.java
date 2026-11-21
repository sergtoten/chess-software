package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.ports.persistence.GameStore;

public class RetrieveGameUseCase implements UseCase {
    private final GameStore store;

    public RetrieveGameUseCase(GameStore store) {
        this.store = store;
    }

    @Override
    public Output execute(Input input) {
        return new Output() {
            @Override
            public int status() {
                return 200;
            }

            @Override
            public byte[] result() {
                return store.load(input.get("id")).getBytes();
            }
        };
    }
}
