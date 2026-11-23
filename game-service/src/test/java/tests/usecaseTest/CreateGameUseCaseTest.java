package tests.usecaseTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.model.Game;
import software.ulpgc.chess.application.model.GameStatus;
import software.ulpgc.chess.application.model.Player;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.usecases.CreateGameUseCase;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenPositionDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.GameJsonDeserializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenBoardSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenPositionSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenStateSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.GameJsonSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CreateGameUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-11-23T10:00:00Z");
    private static final String CODE = "ABC234";

    private final InMemoryGameStore store = new InMemoryGameStore();
    private final InMemoryRoomIndex index = new InMemoryRoomIndex();
    private final GameJsonSerializer serializer =
            new GameJsonSerializer(new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer()));
    private final GameJsonDeserializer deserializer =
            new GameJsonDeserializer(new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer()));
    private final CreateGameUseCase useCase =
            new CreateGameUseCase(store, index, serializer, () -> NOW, () -> Color.White, () -> CODE);

    private UseCase.Output execute(Map<String, String> params) {
        return useCase.execute(params::get);
    }

    private static String body(UseCase.Output output) {
        return new String(output.result(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Sin playerId devuelve 400 y no guarda nada")
    void missingPlayerId() {
        UseCase.Output output = execute(Map.of("playerName", "Ana"));

        assertThat(output.status()).isEqualTo(400);
        assertThat(store.isEmpty()).isTrue();
        assertThat(index.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Sin playerName o con playerName en blanco devuelve 400")
    void missingOrBlankPlayerName() {
        assertThat(execute(Map.of("playerId", "p1")).status()).isEqualTo(400);
        assertThat(execute(Map.of("playerId", "p1", "playerName", "   ")).status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Con un jugador válido devuelve 200 y una partida en espera con el creador en su color")
    void createsWaitingGame() {
        UseCase.Output output = execute(Map.of("playerId", "p1", "playerName", "Ana"));

        assertThat(output.status()).isEqualTo(200);
        Game game = deserializer.deserialize(body(output));
        assertThat(game.status()).isEqualTo(GameStatus.Waiting);
        assertThat(game.white()).contains(new Player("p1", "Ana"));
        assertThat(game.black()).isEmpty();
        assertThat(game.roomCode()).isEqualTo(CODE);
        assertThat(game.createdAt()).isEqualTo(NOW);
        assertThat(game.lastActivity()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("Guarda la partida con su id y registra el código de sala en el índice")
    void savesGameAndRoom() {
        UseCase.Output output = execute(Map.of("playerId", "p1", "playerName", "Ana"));
        Game game = deserializer.deserialize(body(output));

        assertThat(store.load(game.id())).contains(body(output));
        assertThat(index.findGameId(CODE)).contains(game.id());
    }
}
