package software.ulpgc.chess.application.ports.serializer;

import software.ulpgc.chess.application.domain.model.State;

public interface StateSerializer {

    String serialize(State state);

}
