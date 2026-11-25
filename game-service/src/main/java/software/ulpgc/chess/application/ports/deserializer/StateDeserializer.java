package software.ulpgc.chess.application.ports.deserializer;

import software.ulpgc.chess.application.model.position.State;

public interface StateDeserializer {

    State deserialize(String value);

}
