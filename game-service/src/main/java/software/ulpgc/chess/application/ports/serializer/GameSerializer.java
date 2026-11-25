package software.ulpgc.chess.application.ports.serializer;

import software.ulpgc.chess.application.model.game.Game;

public interface GameSerializer {
    String serialize(Game game);
}
