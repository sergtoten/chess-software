package tests.usecaseTest;

import software.ulpgc.chess.application.ports.persistence.RoomIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class InMemoryRoomIndex implements RoomIndex {
    private final Map<String, String> rooms = new HashMap<>();

    @Override
    public void saveRoom(String roomCode, String id) {
        rooms.put(roomCode, id);
    }

    @Override
    public Optional<String> findGameId(String roomCode) {
        return Optional.ofNullable(rooms.get(roomCode));
    }

    boolean isEmpty() {
        return rooms.isEmpty();
    }
}
