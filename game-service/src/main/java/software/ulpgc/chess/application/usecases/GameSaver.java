package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.model.game.Game;
import software.ulpgc.chess.application.ports.persistence.GameStore;
import software.ulpgc.chess.application.ports.serializer.GameSerializer;

import java.nio.charset.StandardCharsets;

final class GameSaver {
    static UseCase.Output saveAndRespond(GameStore store, GameSerializer serializer, Game game) {
        String json = serializer.serialize(game);
        store.save(game.id(), json);
        return new UseCaseOutput(200, json.getBytes(StandardCharsets.UTF_8));
    }
}
