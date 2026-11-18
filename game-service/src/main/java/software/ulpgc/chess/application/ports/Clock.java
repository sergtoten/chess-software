package software.ulpgc.chess.application.ports;

import java.time.Instant;

public interface Clock {

    Instant now();

}
