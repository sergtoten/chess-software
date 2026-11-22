package software.ulpgc.chess.application.ports.persistence;

import java.util.Optional;

public interface RoomIndex {

    void saveRoom(String roomCode, String id);
    Optional<String> findRoom(String roomCode);

}
