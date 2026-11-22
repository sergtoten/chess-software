package software.ulpgc.chess;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import software.ulpgc.chess.application.UseCase;
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
        config.routes.get("/", c->execute(c, new RetrieveGameUseCase(new FileGameStore(new File("games")))));
        config.routes.put("/", c->execute(c, new UpdateGameUseCase()));
        config.routes.post("/", c -> execute(c, new CreateGameUseCase(
                new FileGameStore(new File("games")),
                new FileRoomIndex(new File("games")),
                new GameJsonSerializer(new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer())),
                new SystemClock(), new RandomColorPicker(), new RandomRoomCodeGenerator())));
        config.routes.put("/join", c -> execute(c, new JoinGameUseCase(
                new FileGameStore(new File("games")),
                new SystemClock(),
                new FileRoomIndex(new File("games")),
                new GameJsonDeserializer(new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer())),
                new GameJsonSerializer(new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer())))));
    }

    private static void execute(Context context, UseCase operation) {
        UseCase.Output output = operation.execute(new JavalinContextInput(context));
        context.result(output.result());
        context.status(output.status());
    }

}