package software.ulpgc.chess.application.ports.persistence;

import java.util.Optional;

public interface GameStore {
    /** Devuelve la partida guardada, o vacío si no existe ninguna con ese id. */
    Optional<String> load(String id);
    void save(String id, String game);
}
