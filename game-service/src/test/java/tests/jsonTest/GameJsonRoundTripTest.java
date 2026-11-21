package tests.jsonTest;

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
import software.ulpgc.chess.infrastructure.adapters.deserializer.GameJsonDeserializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenBoardSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenPositionSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenStateSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.GameJsonSerializer;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GameJsonRoundTripTest {

    private static final Instant CREATED = Instant.parse("2026-11-21T10:00:00Z");
    private static final Instant JOINED = Instant.parse("2026-11-21T10:05:00Z");
    private static final Player CREATOR = new Player("uuid-creator", "Ana");
    private static final Player GUEST = new Player("uuid-guest", "Luis");

    private final FenPositionDeserializer fen =
            new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer());
    private final GameJsonSerializer serializer =
            new GameJsonSerializer(new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer()));
    private final GameJsonDeserializer deserializer = new GameJsonDeserializer(fen);

    private Game roundTrip(Game game) {
        return deserializer.deserialize(serializer.serialize(game));
    }

    private Move move(String from, String to) {
        return new Move(Square.at(from), Square.at(to));
    }

    @Test
    @DisplayName("Una partida recién creada (Waiting, un solo jugador) vuelve igual")
    void newGame() {
        Game game = Game.start("game-1", "ROOM42", CREATOR, Color.Black, CREATED);

        assertThat(roundTrip(game)).isEqualTo(game);
    }

    @Test
    @DisplayName("Una partida tras unirse el rival (Ongoing, dos jugadores) vuelve igual")
    void joinedGame() {
        Game game = Game.start("game-1", "ROOM42", CREATOR, Color.White, CREATED).join(GUEST, JOINED);

        assertThat(roundTrip(game)).isEqualTo(game);
    }

    @Test
    @DisplayName("Una partida terminada en jaque mate (mate del pastor invertido) vuelve igual, con su resultado")
    void checkmatedGame() {
        Game game = Game.start("game-1", "ROOM42", CREATOR, Color.White, CREATED).join(GUEST, JOINED)
                .withMove(move("f2", "f3"), fen.deserialize("rnbqkbnr/pppppppp/8/8/8/5P2/PPPPP1PP/RNBQKBNR b KQkq - 0 1"), GameStatus.Ongoing, JOINED)
                .withMove(move("e7", "e5"), fen.deserialize("rnbqkbnr/pppp1ppp/8/4p3/8/5P2/PPPPP1PP/RNBQKBNR w KQkq - 0 2"), GameStatus.Ongoing, JOINED)
                .withMove(move("g2", "g4"), fen.deserialize("rnbqkbnr/pppp1ppp/8/4p3/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq - 0 2"), GameStatus.Ongoing, JOINED)
                .withMove(move("d8", "h4"), fen.deserialize("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3"), GameStatus.Checkmate, JOINED);

        Game back = roundTrip(game);

        assertThat(back).isEqualTo(game);
        assertThat(back.result()).contains(GameResult.BlackWins);
    }

    @Test
    @DisplayName("Las fechas con milisegundos se conservan, y createdAt y lastActivity no se mezclan")
    void datesWithMillisecondsArePreserved() {
        Instant created = Instant.parse("2026-11-21T10:00:00.123Z");
        Instant moved = Instant.parse("2026-11-21T10:07:30.456Z");
        Game game = Game.start("game-1", "ROOM42", CREATOR, Color.White, created).join(GUEST, created)
                .withMove(move("e2", "e4"), fen.deserialize("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"), GameStatus.Ongoing, moved);

        Game back = roundTrip(game);

        assertThat(back).isEqualTo(game);
        assertThat(back.createdAt()).isEqualTo(created);
        assertThat(back.lastActivity()).isEqualTo(moved);
    }
}
