package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;

import java.util.Optional;
import java.util.regex.Pattern;

final class GameIdReader {

    static final String ID = "id";
    private static final Pattern UUID_FORMAT =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    static Optional<String> from(UseCase.Input input) {
        String id = input.get(ID);
        return id != null && UUID_FORMAT.matcher(id).matches() ? Optional.of(id) : Optional.empty();
    }
}
