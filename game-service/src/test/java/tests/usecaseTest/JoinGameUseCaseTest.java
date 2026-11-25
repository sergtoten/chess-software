package tests.usecaseTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.model.game.Game;
import software.ulpgc.chess.application.model.game.GameStatus;
import software.ulpgc.chess.application.model.game.Player;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.usecases.JoinGameUseCase;
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

class JoinGameUseCaseTest {

    private static final Instant CREATED = Instant.parse("2026-11-23T10:00:00Z");
    private static final Instant JOINED = Instant.parse("2026-11-23T10:05:00Z");
    private static final String ID = "123e4567-e89b-42d3-a456-426614174000";
    private static final String CODE = "ABC234";
    private static final Player CREATOR = new Player("p1", "Ana");
    private static final Player GUEST = new Player("p2", "Luis");

    private final InMemoryGameStore store = new InMemoryGameStore();
    private final InMemoryRoomIndex index = new InMemoryRoomIndex();
    private final GameJsonSerializer serializer =
            new GameJsonSerializer(new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer()));
    private final GameJsonDeserializer deserializer =
            new GameJsonDeserializer(new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer()));
    private final JoinGameUseCase useCase = new JoinGameUseCase(store, () -> JOINED, index, deserializer, serializer);

    @BeforeEach
    void createRoom() {
        store.save(ID, serializer.serialize(Game.start(ID, CODE, CREATOR, Color.White, CREATED)));
        index.saveRoom(CODE, ID);
    }

    private UseCase.Output join(Player player, String roomCode) {
        Map<String, String> params = Map.of("playerId", player.id(), "playerName", player.name(), "roomCode", roomCode);
        return useCase.execute(params::get);
    }

    private static String body(UseCase.Output output) {
        return new String(output.result(), StandardCharsets.UTF_8);
    }

    private Game storedGame() {
        return deserializer.deserialize(store.load(ID).orElseThrow());
    }

    @Test
    @DisplayName("Sin jugador devuelve 400")
    void missingPlayer() {
        Map<String, String> params = Map.of("roomCode", CODE);

        assertThat(useCase.execute(params::get).status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Con un roomCode mal formado devuelve 400")
    void malformedRoomCode() {
        assertThat(join(GUEST, "../x").status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Con un código que no está en el índice devuelve 404 'room not found'")
    void unknownRoom() {
        UseCase.Output output = join(GUEST, "ZZZ999");

        assertThat(output.status()).isEqualTo(404);
        assertThat(body(output)).contains("room not found");
    }

    @Test
    @DisplayName("Si el índice apunta a una partida que no está guardada devuelve 404 'game not found'")
    void roomWithoutGame() {
        index.saveRoom("XYZ234", "00000000-0000-4000-8000-000000000000");

        UseCase.Output output = join(GUEST, "XYZ234");

        assertThat(output.status()).isEqualTo(404);
        assertThat(body(output)).contains("game not found");
    }

    @Test
    @DisplayName("Un invitado válido devuelve 200: partida Ongoing, invitado en el color libre y lastActivity del reloj")
    void guestJoins() {
        UseCase.Output output = join(GUEST, CODE);

        assertThat(output.status()).isEqualTo(200);
        Game game = deserializer.deserialize(body(output));
        assertThat(game.status()).isEqualTo(GameStatus.Ongoing);
        assertThat(game.white()).contains(CREATOR);
        assertThat(game.black()).contains(GUEST);
        assertThat(game.createdAt()).isEqualTo(CREATED);
        assertThat(game.lastActivity()).isEqualTo(JOINED);
    }

    @Test
    @DisplayName("Tras unirse, la partida guardada queda actualizada")
    void joinIsSaved() {
        UseCase.Output output = join(GUEST, CODE);

        assertThat(storedGame()).isEqualTo(deserializer.deserialize(body(output)));
        assertThat(storedGame().status()).isEqualTo(GameStatus.Ongoing);
    }

    @Test
    @DisplayName("El creador no puede unirse a su propia sala (409) y la partida sigue en espera")
    void creatorCannotJoinOwnRoom() {
        assertThat(join(CREATOR, CODE).status()).isEqualTo(409);
        assertThat(storedGame().status()).isEqualTo(GameStatus.Waiting);
    }

    @Test
    @DisplayName("Un tercer jugador no puede unirse a una partida ya en curso (409)")
    void thirdPlayerCannotJoin() {
        join(GUEST, CODE);

        assertThat(join(new Player("p3", "Eva"), CODE).status()).isEqualTo(409);
        assertThat(storedGame().black()).contains(GUEST);
    }
}
