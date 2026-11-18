package software.ulpgc.chess.application.ports.deserializer;

import software.ulpgc.chess.application.model.State;

public interface StateDeserializer {

    State deserialize(String value);

}
