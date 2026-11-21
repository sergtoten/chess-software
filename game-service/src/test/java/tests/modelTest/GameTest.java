package tests.modelTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.model.Game;
import software.ulpgc.chess.application.model.GameResult;
import software.ulpgc.chess.application.model.GameStatus;
import software.ulpgc.chess.application.model.Move;
import software.ulpgc.chess.application.model.Player;
import software.ulpgc.chess.application.model.Position;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenPositionDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GameTest {

    private static final Instant CREATED = Instant.parse("2026-11-18T10:00:00Z");
    private static final Instant JOINED = Instant.parse("2026-11-18T10:05:00Z");
    private static final Player CREATOR = new Player("uuid-creator", "Ana");
    private static final Player GUEST = new Player("uuid-guest", "Luis");

    private Game start(Color creatorColor) {
        return Game.start("game-1", "ROOM42", CREATOR, creatorColor, CREATED);
    }

    @Test
    @DisplayName("Al crear con blancas, el creador ocupa blancas, negras queda libre y la partida espera rival")
    void startAsWhite() {
        Game game = start(Color.White);
        assertThat(game.white()).contains(CREATOR);
        assertThat(game.black()).isEmpty();
        assertThat(game.status()).isEqualTo(GameStatus.Waiting);
    }

    @Test
    @DisplayName("Al crear con negras, el creador ocupa negras y blancas queda libre")
    void startAsBlack() {
        Game game = start(Color.Black);
        assertThat(game.white()).isEmpty();
        assertThat(game.black()).contains(CREATOR);
        assertThat(game.status()).isEqualTo(GameStatus.Waiting);
    }

    @Test
    @DisplayName("Si el creador juega con blancas, el invitado se une con negras y la partida empieza")
    void guestJoinsAsBlack() {
        Game game = start(Color.White).join(GUEST, JOINED);
        assertThat(game.white()).contains(CREATOR);
        assertThat(game.black()).contains(GUEST);
        assertThat(game.status()).isEqualTo(GameStatus.Ongoing);
    }

    @Test
    @DisplayName("Si el creador juega con negras, el invitado se une con blancas")
    void guestJoinsAsWhite() {
        Game game = start(Color.Black).join(GUEST, JOINED);
        assertThat(game.white()).contains(GUEST);
        assertThat(game.black()).contains(CREATOR);
        assertThat(game.status()).isEqualTo(GameStatus.Ongoing);
    }

    @Test
    @DisplayName("Unirse conserva id, código, fecha de creación y posición, y solo actualiza la última actividad")
    void joinKeepsGameData() {
        Game game = start(Color.White).join(GUEST, JOINED);
        assertThat(game.id()).isEqualTo("game-1");
        assertThat(game.roomCode()).isEqualTo("ROOM42");
        assertThat(game.createdAt()).isEqualTo(CREATED);
        assertThat(game.lastActivity()).isEqualTo(JOINED);
        assertThat(game.position()).isEqualTo(Position.initial());
        assertThat(game.moves()).isEmpty();
    }

    // ---------- withMove ----------

    private static final Instant MOVED = Instant.parse("2026-11-18T10:06:00Z");
    private static final String AFTER_E4 = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";
    private static final String AFTER_E5 = "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2";
    private static final String FOOLS_MATE = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3";
    private static final String BACK_RANK_MATE = "R5k1/5ppp/8/8/8/8/8/4K3 b - - 0 1";
    private static final String STALEMATE = "7k/5Q2/6K1/8/8/8/8/8 b - - 0 1";

    private final FenPositionDeserializer fen =
            new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer());

    private Game playing() {
        return start(Color.White).join(GUEST, JOINED);
    }

    private Move move(String from, String to) {
        return new Move(Square.at(from), Square.at(to));
    }

    private Game afterLastMove(String fen, GameStatus status) {
        return playing().withMove(move("a1", "a2"), this.fen.deserialize(fen), status, MOVED);
    }

    @Test
    @DisplayName("Un movimiento se añade al historial y actualiza posición, estado y última actividad")
    void moveIsRecorded() {
        Game game = playing().withMove(move("e2", "e4"), fen.deserialize(AFTER_E4), GameStatus.Ongoing, MOVED);
        assertThat(game.moves()).containsExactly(move("e2", "e4"));
        assertThat(game.position()).isEqualTo(fen.deserialize(AFTER_E4));
        assertThat(game.status()).isEqualTo(GameStatus.Ongoing);
        assertThat(game.lastActivity()).isEqualTo(MOVED);
        assertThat(game.createdAt()).isEqualTo(CREATED);
    }

    @Test
    @DisplayName("Mover no modifica la partida original")
    void moveDoesNotModifyOriginalGame() {
        Game before = playing();
        before.withMove(move("e2", "e4"), fen.deserialize(AFTER_E4), GameStatus.Ongoing, MOVED);
        assertThat(before.moves()).isEmpty();
        assertThat(before.position()).isEqualTo(Position.initial());
    }

    @Test
    @DisplayName("Los movimientos quedan en el historial en el orden en que se hicieron")
    void movesAreKeptInOrder() {
        Game game = playing()
                .withMove(move("e2", "e4"), fen.deserialize(AFTER_E4), GameStatus.Ongoing, MOVED)
                .withMove(move("e7", "e5"), fen.deserialize(AFTER_E5), GameStatus.Ongoing, MOVED);
        assertThat(game.moves()).containsExactly(move("e2", "e4"), move("e7", "e5"));
    }

    @Test
    @DisplayName("Si las blancas reciben mate, ganan las negras")
    void checkmateOnWhiteMeansBlackWins() {
        Game game = afterLastMove(FOOLS_MATE, GameStatus.Checkmate);
        assertThat(game.result()).contains(GameResult.BlackWins);
    }

    @Test
    @DisplayName("Si las negras reciben mate, ganan las blancas")
    void checkmateOnBlackMeansWhiteWins() {
        Game game = afterLastMove(BACK_RANK_MATE, GameStatus.Checkmate);
        assertThat(game.result()).contains(GameResult.WhiteWins);
    }

    @Test
    @DisplayName("El rey ahogado termina en tablas")
    void stalemateIsDraw() {
        Game game = afterLastMove(STALEMATE, GameStatus.Stalemate);
        assertThat(game.result()).contains(GameResult.Draw);
    }

    @Test
    @DisplayName("Mientras la partida sigue (en curso o en jaque) no hay resultado")
    void noResultWhileGameContinues() {
        assertThat(afterLastMove(AFTER_E4, GameStatus.Ongoing).result()).isEmpty();
        assertThat(afterLastMove(AFTER_E4, GameStatus.Check).result()).isEmpty();
    }
}
