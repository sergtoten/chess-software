package software.ulpgc.chess.application.ports.persistence;

public interface GameStore {
    /** Devuelve la partida guardada, o null si no existe ninguna con ese id. */
    String load(String id);
    void save(String id, String game);
}
