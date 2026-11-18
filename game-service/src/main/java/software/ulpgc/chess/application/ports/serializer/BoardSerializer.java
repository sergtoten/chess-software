package software.ulpgc.chess.application.ports.serializer;

import software.ulpgc.chess.application.model.Board;

public interface BoardSerializer {

    String serialize(Board board);

}
