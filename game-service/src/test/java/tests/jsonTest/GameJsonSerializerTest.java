package tests.jsonTest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.model.game.Game;
import software.ulpgc.chess.application.model.game.Player;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenBoardSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenPositionSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenStateSerializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.GameJsonSerializer;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GameJsonSerializerTest {

    private static final Instant CREATED = Instant.parse("2026-11-21T10:00:00Z");
    private static final Player CREATOR = new Player("uuid-creator", "Ana");

    private final GameJsonSerializer serializer =
            new GameJsonSerializer(new FenPositionSerializer(new FenBoardSerializer(), new FenStateSerializer()));

    private JsonObject serialize(Game game) {
        return JsonParser.parseString(serializer.serialize(game)).getAsJsonObject();
    }

    @Test
    @DisplayName("Si el creador juega con blancas, el JSON tiene 'white' con id y nombre, y no tiene 'black'")
    void creatorAsWhiteOnlyWritesWhite() {
        JsonObject json = serialize(Game.start("game-1", "ROOM42", CREATOR, Color.White, CREATED));

        JsonObject white = json.getAsJsonObject("white");
        assertThat(white.get("id").getAsString()).isEqualTo("uuid-creator");
        assertThat(white.get("name").getAsString()).isEqualTo("Ana");
        assertThat(json.has("black")).isFalse();
    }

    @Test
    @DisplayName("Una partida sin resultado no escribe la clave 'result'")
    void gameWithoutResultHasNoResultKey() {
        JsonObject json = serialize(Game.start("game-1", "ROOM42", CREATOR, Color.White, CREATED));

        assertThat(json.get("status").getAsString()).isEqualTo("Waiting");
        assertThat(json.has("result")).isFalse();
    }
}
