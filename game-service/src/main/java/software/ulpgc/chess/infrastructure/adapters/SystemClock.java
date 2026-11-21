package software.ulpgc.chess.infrastructure.adapters;

import software.ulpgc.chess.application.ports.Clock;

import java.time.Instant;

public class SystemClock implements Clock {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
