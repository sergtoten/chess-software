package software.ulpgc.chess.application.ports;

public interface GameStore {
    String load(String id);
    void save(String id, String game);
}
