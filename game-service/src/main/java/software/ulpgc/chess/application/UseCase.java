package software.ulpgc.chess.application;

public interface UseCase {

    Output execute(Input input);

    interface Input {
        String get(String key);
    }

    interface Output {
        int status();
        byte[] result();

    }
}
