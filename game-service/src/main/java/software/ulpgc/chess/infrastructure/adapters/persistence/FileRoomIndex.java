package software.ulpgc.chess.infrastructure.adapters.persistence;

import software.ulpgc.chess.application.ports.persistence.RoomIndex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class FileRoomIndex implements RoomIndex {

    private final File root;

    public FileRoomIndex(File root) {
        this.root = root;
    }

    @Override
    public void saveRoom(String roomCode, String id) {
        try {
            File file = toFile(roomCode);
            Files.createDirectories(file.getParentFile().toPath());
            Files.write(file.toPath(), id.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<String> findRoom(String roomCode) {
        File file = toFile(roomCode);
        if (!file.exists()) return Optional.empty();
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            return Optional.of(new String(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File toFile(String id) {
        return new File(this.root, id + ".txt");
    }

}
