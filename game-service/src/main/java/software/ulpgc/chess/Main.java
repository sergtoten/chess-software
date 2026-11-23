package software.ulpgc.chess;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.ports.Clock;
import software.ulpgc.chess.application.ports.deserializer.GameDeserializer;
import software.ulpgc.chess.application.ports.persistence.GameStore;
import software.ulpgc.chess.application.ports.persistence.RoomIndex;
import software.ulpgc.chess.application.ports.serializer.GameSerializer;
import software.ulpgc.chess.application.usecases.CreateGameUseCase;
import software.ulpgc.chess.application.usecases.JoinGameUseCase;
import software.ulpgc.chess.application.usecases.RetrieveGameUseCase;
import software.ulpgc.chess.application.usecases.UpdateGameUseCase;
import software.ulpgc.chess.infrastructure.adapters.JavalinContextInput;
import software.ulpgc.chess.infrastructure.adapters.RandomColorPicker;
import software.ulpgc.chess.infrastructure.adapters.RandomRoomCodeGenerator;
import software.ulpgc.chess.infrastructure.adapters.SystemClock;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenPositionDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.GameJsonDeserializer;
import software.ulpgc.chess.infrastructure.adapters.persistence.FileGameStore;
import software.ulpgc.chess.infrastructure.adapters.persistence.FileRoomIndex;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenBoardSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenPositionSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenStateSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.GameJsonSerializer;

import java.io.File;

public class Main {

    static void main() {
        Javalin.create(Main::init).start(8080);
    }

    private static void init(JavalinConfig config) {
        GameStore store = new FileGameStore(new File("games"));
        RoomIndex index = new FileRoomIndex(new File("games/rooms"));
        GameSerializer serializer = new GameJsonSerializer(new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer()));
        GameDeserializer deserializer = new GameJsonDeserializer(new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer()));
        Clock clock = new SystemClock();

        UseCase retrieve = new RetrieveGameUseCase(store);
        UseCase update = new UpdateGameUseCase();
        UseCase create = new CreateGameUseCase(store, index, serializer, clock, new RandomColorPicker(), new RandomRoomCodeGenerator());
        UseCase join = new JoinGameUseCase(store, clock, index, deserializer, serializer);

        config.routes.get("/", c -> execute(c, retrieve));
        config.routes.put("/", c -> execute(c, update));
        config.routes.post("/", c -> execute(c, create));
        config.routes.put("/join", c -> execute(c, join));
    }

    private static void execute(Context context, UseCase operation) {
        UseCase.Output output = operation.execute(new JavalinContextInput(context));
        context.contentType("application/json");
        context.result(output.result());
        context.status(output.status());
    }

}