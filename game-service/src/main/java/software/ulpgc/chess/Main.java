package software.ulpgc.chess;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.usecases.CreateGameUseCase;
import software.ulpgc.chess.application.usecases.RetrieveGameUseCase;
import software.ulpgc.chess.application.usecases.UpdateGameUseCase;
import software.ulpgc.chess.infrastructure.adapters.JavalinContextInput;
import software.ulpgc.chess.infrastructure.adapters.RandomColorPicker;
import software.ulpgc.chess.infrastructure.adapters.RamdonRoomCodeGenerator;
import software.ulpgc.chess.infrastructure.adapters.SystemClock;
import software.ulpgc.chess.infrastructure.adapters.persistence.FileGameStore;

import java.io.File;

public class Main {

    static void main() {
        Javalin.create(Main::init).start(8080);
    }

    private static void init(JavalinConfig config) {
        config.routes.get("/", c->execute(c, new RetrieveGameUseCase(new FileGameStore(new File("games")))));
        config.routes.put("/", c->execute(c, new UpdateGameUseCase()));
        config.routes.post("/", c -> execute(c, new CreateGameUseCase(
                new FileGameStore(new File("games")), new SystemClock(), new RandomColorPicker(), new RamdonRoomCodeGenerator())));
    }

    private static void execute(Context context, UseCase operation) {
        UseCase.Output output = operation.execute(new JavalinContextInput(context));
        context.result(output.result());
        context.status(output.status());
    }

}