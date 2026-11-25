package tests.jsonTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.model.game.Game;
import software.ulpgc.chess.application.model.position.Move;
import software.ulpgc.chess.application.model.game.PlayedMove;
import software.ulpgc.chess.application.model.game.Player;
import software.ulpgc.chess.application.model.board.Square;

import java.util.Optional;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenBoardDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenPositionDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;
import software.ulpgc.chess.infrastructure.adapters.deserializer.GameJsonDeserializer;

import static org.assertj.core.api.Assertions.assertThat;

class GameJsonDeserializerTest {

    private final GameJsonDeserializer deserializer =
            new GameJsonDeserializer(new FenPositionDeserializer(new FenBoardDeserializer(), new FenStateDeserializer()));

    @Test
    @DisplayName("Si faltan 'black' y 'result', quedan como Optional vacío (no null)")
    void missingBlackAndResultAreEmpty() {
        String json = """
                {"id":"game-1","roomCode":"ROOM42",
                 "white":{"id":"uuid-creator","name":"Ana"},
                 "position":"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                 "moves":[],"status":"Waiting",
                 "createdAt":"2026-11-21T10:00:00Z","lastActivity":"2026-11-21T10:00:00Z"}
                """;

        Game game = deserializer.deserialize(json);

        assertThat(game.white()).contains(new Player("uuid-creator", "Ana"));
        assertThat(game.black()).isNotNull().isEmpty();
        assertThat(game.result()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("El movimiento \"e7e8\" se lee como Move(e7, e8)")
    void moveTextIsReadAsFromAndTo() {
        String json = """
                {"id":"game-1","roomCode":"ROOM42",
                 "white":{"id":"uuid-creator","name":"Ana"},
                 "black":{"id":"uuid-guest","name":"Luis"},
                 "position":"4k3/4P3/8/8/8/8/8/4K3 w - - 0 1",
                 "moves":["e7e8"],"status":"Ongoing",
                 "createdAt":"2026-11-21T10:00:00Z","lastActivity":"2026-11-21T10:00:00Z"}
                """;

        Game game = deserializer.deserialize(json);

        assertThat(game.moves()).containsExactly(new PlayedMove(new Move(Square.at("e7"), Square.at("e8")), Optional.empty()));
    }
}
