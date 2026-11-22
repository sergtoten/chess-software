package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;

import java.util.Optional;
import java.util.regex.Pattern;

final class RoomCodeReader {

    static final String ROOMCODE = "roomCode";

    private static final Pattern ROOMCODE_FORMAT =
            Pattern.compile("[A-HJ-NP-Z2-9]{6}");

    static Optional<String> from(UseCase.Input input) {
        String roomCode = input.get(ROOMCODE);
        return roomCode != null && ROOMCODE_FORMAT.matcher(roomCode).matches() ? Optional.of(roomCode) : Optional.empty();
    }

}
