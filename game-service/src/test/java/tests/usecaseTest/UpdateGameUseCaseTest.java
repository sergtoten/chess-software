package tests.usecaseTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.domain.service.CastlingMoveGenerator;
import software.ulpgc.chess.application.domain.service.GameRules;
import software.ulpgc.chess.application.domain.service.MoveApplier;
import software.ulpgc.chess.application.domain.service.MoveGenerate;
import software.ulpgc.chess.application.domain.service.PawnMoveGenerate;
import software.ulpgc.chess.application.model.game.Game;
import software.ulpgc.chess.application.model.game.GameResult;
import software.ulpgc.chess.application.model.game.GameStatus;
import software.ulpgc.chess.application.model.game.Player;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Piece;
import software.ulpgc.chess.application.usecases.UpdateGameUseCase;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateGameUseCaseTest {

    private static final Instant CREATED = Instant.parse("2026-11-25T10:00:00Z");
    private static final Instant JOINED = Instant.parse("2026-11-25T10:05:00Z");
    private static final Instant MOVED = Instant.parse("2026-11-25T10:10:00Z");
    private static final String ID = "123e4567-e89b-42d3-a456-426614174000";
    private static final String CODE = "ABC234";
    private static final Player WHITE = new Player("p1", "Ana");
    private static final Player BLACK = new Player("p2", "Luis");
    private static final String PROMOTION_FEN = "4k3/P7/8/8/8/8/8/4K3 w - - 0 1";

    private final InMemoryGameStore store = new InMemoryGameStore();
    private final GameJsonSerializer serializer =
            new GameJsonSerializer(new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer()));
    private final FenPositionDeserializer fen =
            new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer());
    private final GameJsonDeserializer deserializer = new GameJsonDeserializer(fen);
    private final GameRules rules = new GameRules(new MoveGenerate(new PawnMoveGenerate()), new CastlingMoveGenerator());
    private final UpdateGameUseCase useCase =
            new UpdateGameUseCase(store, () -> MOVED, deserializer, serializer, new MoveApplier(rules), rules);

    private static Game waitingGame() {
        return Game.start(ID, CODE, WHITE, Color.White, CREATED);
    }

    private static Game ongoingGame() {
        return waitingGame().join(BLACK, JOINED);
    }

    private Game ongoingGameFrom(String fenPosition) {
        return new Game(ID, CODE, Optional.of(WHITE), Optional.of(BLACK), fen.deserialize(fenPosition), List.of(),
                GameStatus.Ongoing, Optional.empty(), CREATED, JOINED);
    }

    private void saved(Game game) {
        store.save(ID, serializer.serialize(game));
    }

    private Game storedGame() {
        return deserializer.deserialize(store.load(ID).orElseThrow());
    }

    private UseCase.Output move(Player player, String move) {
        return execute(Map.of("id", ID, "playerId", player.id(), "playerName", player.name(), "move", move));
    }

    private UseCase.Output move(Player player, String move, String promotion) {
        Map<String, String> params = new HashMap<>(Map.of("id", ID, "playerId", player.id(), "playerName", player.name(), "move", move));
        params.put("promotion", promotion);
        return execute(params);
    }

    private UseCase.Output execute(Map<String, String> params) {
        return useCase.execute(params::get);
    }

    private static String body(UseCase.Output output) {
        return new String(output.result(), StandardCharsets.UTF_8);
    }

    // ---------- movimientos válidos ----------

    @Test
    @DisplayName("Movimiento legal de blancas: 200, queda en el historial, turno de negras, Ongoing, guardada y lastActivity del reloj")
    void legalMove() {
        saved(ongoingGame());

        UseCase.Output output = move(WHITE, "e2e4");

        assertThat(output.status()).isEqualTo(200);
        assertThat(body(output)).contains("\"moves\":[\"e2e4\"]");
        Game game = deserializer.deserialize(body(output));
        assertThat(game.position().turn()).isEqualTo(Color.Black);
        assertThat(game.status()).isEqualTo(GameStatus.Ongoing);
        assertThat(game.result()).isEmpty();
        assertThat(game.lastActivity()).isEqualTo(MOVED);
        assertThat(storedGame()).isEqualTo(game);
    }

    @Test
    @DisplayName("Un movimiento que da jaque deja la partida en Check")
    void moveGivesCheck() {
        saved(ongoingGame());
        move(WHITE, "e2e4");
        move(BLACK, "f7f6");

        UseCase.Output output = move(WHITE, "d1h5");

        assertThat(output.status()).isEqualTo(200);
        assertThat(storedGame().status()).isEqualTo(GameStatus.Check);
    }

    @Test
    @DisplayName("Mate del loco: Checkmate y ganan las negras")
    void foolsMate() {
        saved(ongoingGame());
        move(WHITE, "f2f3");
        move(BLACK, "e7e5");
        move(WHITE, "g2g4");

        UseCase.Output output = move(BLACK, "d8h4");

        assertThat(output.status()).isEqualTo(200);
        assertThat(storedGame().status()).isEqualTo(GameStatus.Checkmate);
        assertThat(storedGame().result()).contains(GameResult.BlackWins);
        assertThat(body(output)).contains("\"moves\":[\"f2f3\",\"e7e5\",\"g2g4\",\"d8h4\"]");
    }

    @Test
    @DisplayName("Estando en jaque se puede mover para taparlo: 200 y vuelve a Ongoing")
    void moveOutOfCheck() {
        saved(ongoingGame());
        move(WHITE, "e2e4");
        move(BLACK, "f7f6");
        move(WHITE, "d1h5");

        UseCase.Output output = move(BLACK, "g7g6");

        assertThat(output.status()).isEqualTo(200);
        assertThat(storedGame().status()).isEqualTo(GameStatus.Ongoing);
    }

    @Test
    @DisplayName("Promoción con promotion=Queen: 200 y hay una dama blanca en a8")
    void promotionToQueen() {
        saved(ongoingGameFrom(PROMOTION_FEN));

        UseCase.Output output = move(WHITE, "a7a8", "Queen");

        assertThat(output.status()).isEqualTo(200);
        assertThat(body(output)).contains("\"position\":\"Q3k3/8/8/8/8/8/8/4K3 b");
        assertThat(body(output)).contains("\"moves\":[\"a7a8q\"]");
        assertThat(storedGame().moves().getFirst().promotion()).contains(Piece.WhiteQueen);
    }

    @Test
    @DisplayName("Movimiento de promoción sin indicar la pieza: 422 y la partida no cambia")
    void promotionWithoutPiece() {
        Game game = ongoingGameFrom(PROMOTION_FEN);
        saved(game);

        assertThat(move(WHITE, "a7a8").status()).isEqualTo(422);
        assertThat(storedGame()).isEqualTo(game);
    }

    // ---------- 400: entrada incompleta o mal formada ----------

    @Test
    @DisplayName("Sin id o con id mal formado devuelve 400")
    void missingOrMalformedId() {
        saved(ongoingGame());
        Map<String, String> noId = Map.of("playerId", WHITE.id(), "playerName", WHITE.name(), "move", "e2e4");
        Map<String, String> badId = Map.of("id", "../x", "playerId", WHITE.id(), "playerName", WHITE.name(), "move", "e2e4");

        assertThat(execute(noId).status()).isEqualTo(400);
        assertThat(execute(badId).status()).isEqualTo(400);
        assertThat(storedGame()).isEqualTo(ongoingGame());
    }

    @Test
    @DisplayName("Sin jugador devuelve 400")
    void missingPlayer() {
        saved(ongoingGame());

        assertThat(execute(Map.of("id", ID, "move", "e2e4")).status()).isEqualTo(400);
        assertThat(storedGame()).isEqualTo(ongoingGame());
    }

    @Test
    @DisplayName("Movimiento mal formado (e2e9) o promoción no válida (King) devuelve 400")
    void malformedMoveOrPromotion() {
        saved(ongoingGame());

        assertThat(move(WHITE, "e2e9").status()).isEqualTo(400);
        assertThat(move(WHITE, "e2e4", "King").status()).isEqualTo(400);
        assertThat(storedGame()).isEqualTo(ongoingGame());
    }

    // ---------- 404 / 409 / 403 / 422 ----------

    @Test
    @DisplayName("Partida inexistente devuelve 404")
    void unknownGame() {
        UseCase.Output output = move(WHITE, "e2e4");

        assertThat(output.status()).isEqualTo(404);
        assertThat(body(output)).contains("game not found");
        assertThat(store.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("En espera de rival (Waiting) devuelve 409 y la partida no cambia")
    void moveWhileWaiting() {
        saved(waitingGame());

        assertThat(move(WHITE, "e2e4").status()).isEqualTo(409);
        assertThat(storedGame()).isEqualTo(waitingGame());
    }

    @Test
    @DisplayName("En una partida terminada (mate o rendición) devuelve 409")
    void moveInFinishedGame() {
        saved(ongoingGame());
        move(WHITE, "f2f3");
        move(BLACK, "e7e5");
        move(WHITE, "g2g4");
        move(BLACK, "d8h4");
        assertThat(move(WHITE, "a2a3").status()).isEqualTo(409);

        saved(ongoingGame().resign(Color.Black, MOVED));
        assertThat(move(WHITE, "e2e4").status()).isEqualTo(409);
    }

    @Test
    @DisplayName("Un tercero que no juega la partida devuelve 403 y la partida no cambia")
    void thirdPlayerCannotMove() {
        saved(ongoingGame());

        assertThat(move(new Player("p3", "Eva"), "e2e4").status()).isEqualTo(403);
        assertThat(storedGame()).isEqualTo(ongoingGame());
    }

    @Test
    @DisplayName("Mover fuera de turno (negras primero) devuelve 409 y la partida no cambia")
    void moveOutOfTurn() {
        saved(ongoingGame());

        UseCase.Output output = move(BLACK, "e7e5");

        assertThat(output.status()).isEqualTo(409);
        assertThat(body(output)).contains("not your turn");
        assertThat(storedGame()).isEqualTo(ongoingGame());
    }

    @Test
    @DisplayName("Movimiento ilegal (e2e5) devuelve 422 y la partida no cambia")
    void illegalMove() {
        saved(ongoingGame());

        UseCase.Output output = move(WHITE, "e2e5");

        assertThat(output.status()).isEqualTo(422);
        assertThat(body(output)).contains("illegal move");
        assertThat(storedGame()).isEqualTo(ongoingGame());
    }
}
