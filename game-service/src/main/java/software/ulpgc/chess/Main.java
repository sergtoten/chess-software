package software.ulpgc.chess;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.domain.service.CastlingMoveGenerator;
import software.ulpgc.chess.application.domain.service.GameRules;
import software.ulpgc.chess.application.domain.service.MoveApplier;
import software.ulpgc.chess.application.domain.service.MoveGenerate;
import software.ulpgc.chess.application.domain.service.PawnMoveGenerate;
import software.ulpgc.chess.application.ports.Clock;
import software.ulpgc.chess.application.ports.deserializer.GameDeserializer;
import software.ulpgc.chess.application.ports.persistence.GameStore;
import software.ulpgc.chess.application.ports.persistence.RoomIndex;
import software.ulpgc.chess.application.ports.serializer.GameSerializer;
import software.ulpgc.chess.application.usecases.*;
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
        int port = Integer.parseInt(envOrDefault("PORT", "8080"));
        Javalin.create(Main::init).start(port);
    }

    private static void init(JavalinConfig config) {
        File dataDir = new File(envOrDefault("DATA_DIR", "games"));
        GameStore store = new FileGameStore(dataDir);
        RoomIndex index = new FileRoomIndex(new File(dataDir, "rooms"));
        GameSerializer serializer = new GameJsonSerializer(new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer()));
        GameDeserializer deserializer = new GameJsonDeserializer(new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer()));
        Clock clock = new SystemClock();
        GameRules rules = new GameRules(new MoveGenerate(new PawnMoveGenerate()), new CastlingMoveGenerator());
        MoveApplier applier = new MoveApplier(rules);

        UseCase retrieve = new RetrieveGameUseCase(store);
        UseCase update = new UpdateGameUseCase(store, clock, deserializer, serializer, applier, rules);
        UseCase create = new CreateGameUseCase(store, index, serializer, clock, new RandomColorPicker(), new RandomRoomCodeGenerator());
        UseCase join = new JoinGameUseCase(store, clock, index, deserializer, serializer);
        UseCase resign = new ResignGameUseCase(store, clock, deserializer, serializer);

        config.routes.get("/", c -> execute(c, retrieve));
        config.routes.put("/", c -> execute(c, update));
        config.routes.post("/", c -> execute(c, create));
        config.routes.put("/join", c -> execute(c, join));
        config.routes.put("/resign", c -> execute(c, resign));

        String corsOrigin = System.getenv("CORS_ORIGIN");
        if (corsOrigin != null)
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.allowHost(corsOrigin)));
    }

    private static void execute(Context context, UseCase operation) {
        UseCase.Output output = operation.execute(new JavalinContextInput(context));
        context.contentType("application/json");
        context.result(output.result());
        context.status(output.status());
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value != null ? value : defaultValue;
    }

}