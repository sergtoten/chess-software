package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.model.Game;
import software.ulpgc.chess.application.model.Player;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.ports.Clock;
import software.ulpgc.chess.application.ports.CodeGenerator;
import software.ulpgc.chess.application.ports.ColorPicker;
import software.ulpgc.chess.application.ports.persistence.GameStore;
import software.ulpgc.chess.application.ports.persistence.RoomIndex;
import software.ulpgc.chess.application.ports.serializer.GameSerializer;

import java.util.Optional;
import java.util.UUID;

public class CreateGameUseCase implements UseCase {
    private final GameStore store;
    private final RoomIndex index;
    private final GameSerializer serializer;
    private final Clock clock;
    private final ColorPicker colorPicker;
    private final CodeGenerator codeGenerator;
    public CreateGameUseCase(GameStore store, RoomIndex index, GameSerializer serializer, Clock clock, ColorPicker colorPicker, CodeGenerator codeGenerator) {
        this.store = store;
        this.index = index;
        this.serializer = serializer;
        this.clock = clock;
        this.colorPicker = colorPicker;
        this.codeGenerator = codeGenerator;
    }
    @Override
    public Output execute(Input input) {
        Optional<Player> creator = PlayerReader.from(input);
        if (creator.isEmpty()) { return UseCaseOutput.error(400, "missing player or playerID");}
        String id = UUID.randomUUID().toString();
        String roomCode = codeGenerator.generateCode();
        Color colorPlayer1 = colorPicker.pick();
        Game game = Game.start(id, roomCode, creator.get(), colorPlayer1, clock.now());
        String gameSerial = serializer.serialize(game);
        store.save(id, gameSerial);
        index.saveRoom(roomCode, id);
        return new UseCaseOutput(200, gameSerial.getBytes());
    }
}
