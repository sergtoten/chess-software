package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.model.Game;
import software.ulpgc.chess.application.model.Player;
import software.ulpgc.chess.application.ports.Clock;
import software.ulpgc.chess.application.ports.CodeGenerator;
import software.ulpgc.chess.application.ports.ColorPicker;
import software.ulpgc.chess.application.ports.persistence.GameStore;

import java.util.Optional;
import java.util.UUID;

public class CreateGameUseCase implements UseCase {
    private final GameStore store;
    private final Clock clock;
    private final ColorPicker colorPicker;
    private final CodeGenerator codeGenerator;
    public CreateGameUseCase(GameStore store, Clock clock, ColorPicker colorPicker, CodeGenerator codeGenerator) {
        this.store = store;
        this.clock = clock;
        this.colorPicker = colorPicker;
        this.codeGenerator = codeGenerator;
    }

    @Override
    public Output execute(Input input) {
        String id = UUID.randomUUID().toString();
        String roomCode = codeGenerator.generateCode();
        Optional<Player> creator = PlayerReader.from(input);
        if (creator.isEmpty()) {
            return new  Output() {
                @Override
                public int status() {return 400;}

                @Override
                public byte[] result() {return new byte[0];}
            };
        }
        Game game = Game.start(id, roomCode, creator.get(), colorPicker.pick(), clock.now());
        store.save(id, "");
        return new Output() {
            @Override
            public int status() {
                return 200;
            }

            @Override
            public byte[] result() {
                return (id+";"+roomCode).getBytes();
            }
        };
    }
}
