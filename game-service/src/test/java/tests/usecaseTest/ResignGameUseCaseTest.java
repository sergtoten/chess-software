package tests.usecaseTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.model.game.Game;
import software.ulpgc.chess.application.model.game.GameResult;
import software.ulpgc.chess.application.model.game.GameStatus;
import software.ulpgc.chess.application.model.game.Player;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.usecases.ResignGameUseCase;
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

class ResignGameUseCaseTest {

    private static final Instant CREATED = Instant.parse("2026-11-25T10:00:00Z");
    private static final Instant JOINED = Instant.parse("2026-11-25T10:05:00Z");
    private static final Instant RESIGNED = Instant.parse("2026-11-25T10:30:00Z");
    private static final String ID = "123e4567-e89b-42d3-a456-426614174000";
    private static final String CODE = "ABC234";
    private static final Player WHITE = new Player("p1", "Ana");
    private static final Player BLACK = new Player("p2", "Luis");

    private final InMemoryGameStore store = new InMemoryGameStore();
    private final GameJsonSerializer serializer =
            new GameJsonSerializer(new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer()));
    private final GameJsonDeserializer deserializer =
            new GameJsonDeserializer(new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer()));
    private final ResignGameUseCase useCase = new ResignGameUseCase(store, () -> RESIGNED, deserializer, serializer);

    private static Game waitingGame() {
        return Game.start(ID, CODE, WHITE, Color.White, CREATED);
    }

    private static Game ongoingGame() {
        return waitingGame().join(BLACK, JOINED);
    }

    private static Game withStatus(Game game, GameStatus status) {
        return new Game(game.id(), game.roomCode(), game.white(), game.black(), game.position(), game.moves(),
                status, game.result(), game.createdAt(), game.lastActivity());
    }

    private void saved(Game game) {
        store.save(ID, serializer.serialize(game));
    }

    private Game storedGame() {
        return deserializer.deserialize(store.load(ID).orElseThrow());
    }

    private UseCase.Output resign(Player player) {
        Map<String, String> params = Map.of("id", ID, "playerId", player.id(), "playerName", player.name());
        return useCase.execute(params::get);
    }

    private static String body(UseCase.Output output) {
        return new String(output.result(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Se rinden las blancas: 200, Resigned, BlackWins, guardada y lastActivity del reloj")
    void whiteResigns() {
        saved(ongoingGame());

        UseCase.Output output = resign(WHITE);

        assertThat(output.status()).isEqualTo(200);
        Game game = deserializer.deserialize(body(output));
        assertThat(game.status()).isEqualTo(GameStatus.Resigned);
        assertThat(game.result()).contains(GameResult.BlackWins);
        assertThat(game.lastActivity()).isEqualTo(RESIGNED);
        assertThat(storedGame()).isEqualTo(game);
    }

    @Test
    @DisplayName("Se rinden las negras: 200 y WhiteWins")
    void blackResigns() {
        saved(ongoingGame());

        UseCase.Output output = resign(BLACK);

        assertThat(output.status()).isEqualTo(200);
        assertThat(deserializer.deserialize(body(output)).result()).contains(GameResult.WhiteWins);
    }

    @Test
    @DisplayName("Se puede rendir sin ser su turno (negras con turno de blancas): 200")
    void resignOutOfTurn() {
        Game game = ongoingGame();
        assertThat(game.position().turn()).isEqualTo(Color.White);
        saved(game);

        assertThat(resign(BLACK).status()).isEqualTo(200);
        assertThat(storedGame().status()).isEqualTo(GameStatus.Resigned);
    }

    @Test
    @DisplayName("Se puede rendir estando en jaque: 200")
    void resignInCheck() {
        saved(withStatus(ongoingGame(), GameStatus.Check));

        assertThat(resign(WHITE).status()).isEqualTo(200);
        assertThat(storedGame().result()).contains(GameResult.BlackWins);
    }

    @Test
    @DisplayName("Sin id o con id mal formado devuelve 400")
    void missingOrMalformedId() {
        saved(ongoingGame());
        Map<String, String> noId = Map.of("playerId", WHITE.id(), "playerName", WHITE.name());
        Map<String, String> badId = Map.of("id", "../x", "playerId", WHITE.id(), "playerName", WHITE.name());

        assertThat(useCase.execute(noId::get).status()).isEqualTo(400);
        assertThat(useCase.execute(badId::get).status()).isEqualTo(400);
        assertThat(storedGame().status()).isEqualTo(GameStatus.Ongoing);
    }

    @Test
    @DisplayName("Sin jugador devuelve 400")
    void missingPlayer() {
        saved(ongoingGame());
        Map<String, String> params = Map.of("id", ID);

        assertThat(useCase.execute(params::get).status()).isEqualTo(400);
        assertThat(storedGame().status()).isEqualTo(GameStatus.Ongoing);
    }

    @Test
    @DisplayName("Partida inexistente devuelve 404")
    void unknownGame() {
        UseCase.Output output = resign(WHITE);

        assertThat(output.status()).isEqualTo(404);
        assertThat(body(output)).contains("game not found");
        assertThat(store.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("En espera de rival (Waiting) devuelve 409 y la partida no cambia")
    void resignWhileWaiting() {
        saved(waitingGame());

        assertThat(resign(WHITE).status()).isEqualTo(409);
        assertThat(storedGame()).isEqualTo(waitingGame());
    }

    @Test
    @DisplayName("En una partida ya terminada devuelve 409 y el resultado no cambia")
    void resignFinishedGame() {
        saved(ongoingGame());
        resign(WHITE);

        assertThat(resign(BLACK).status()).isEqualTo(409);
        assertThat(storedGame().result()).contains(GameResult.BlackWins);
    }

    @Test
    @DisplayName("Un tercero que no juega la partida devuelve 403 y la partida no cambia")
    void thirdPlayerCannotResign() {
        saved(ongoingGame());

        assertThat(resign(new Player("p3", "Eva")).status()).isEqualTo(403);
        assertThat(storedGame()).isEqualTo(ongoingGame());
    }
}
