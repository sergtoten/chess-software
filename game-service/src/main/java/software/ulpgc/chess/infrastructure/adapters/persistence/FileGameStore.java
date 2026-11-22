package software.ulpgc.chess.infrastructure.adapters.persistence;

import software.ulpgc.chess.application.ports.persistence.GameStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class FileGameStore implements GameStore {
    private final File root;

    public FileGameStore(File root) {
        this.root = root;
    }


    @Override
    public Optional<String> load(String id) {
        File file = toFile(id);
        if (!file.exists()) return Optional.empty();
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            return Optional.of(new String(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(String id, String game) {
        try {
            File file = toFile(id);                                   
            Files.createDirectories(file.getParentFile().toPath());
            Files.write(file.toPath(), game.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File toFile(String id) {
        File current = this.root;
        for (int i = 0; i < 9; i += 3)
            current = new File(current, id.substring(0, i + 3));
        return new File(current, id + ".json");
    }
}
