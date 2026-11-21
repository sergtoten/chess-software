package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.ports.persistence.GameStore;

import java.util.UUID;

public class CreateGameUseCase implements UseCase {
    private final GameStore store;

    public CreateGameUseCase(GameStore store) {
        this.store = store;
    }

    @Override
    public Output execute(Input input) {
        String id = UUID.randomUUID().toString();
        store.save(id, "");
        return new Output() {
            @Override
            public int status() {
                return 200;
            }

            @Override
            public byte[] result() {
                return id.getBytes();
            }
        };
    }
}
