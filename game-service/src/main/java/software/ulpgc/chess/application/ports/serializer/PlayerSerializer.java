package software.ulpgc.chess.application.ports.serializer;

import software.ulpgc.chess.application.model.Player;

public interface PlayerSerializer {
    String serialize(Player player);
}
