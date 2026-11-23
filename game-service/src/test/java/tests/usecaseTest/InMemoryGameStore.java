package tests.usecaseTest;

import software.ulpgc.chess.application.ports.persistence.GameStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class InMemoryGameStore implements GameStore {
    private final Map<String, String> games = new HashMap<>();

    @Override
    public Optional<String> load(String id) {
        return Optional.ofNullable(games.get(id));
    }

    @Override
    public void save(String id, String game) {
        games.put(id, game);
    }

    boolean isEmpty() {
        return games.isEmpty();
    }
}
