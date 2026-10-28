package software.ulpgc.chess.application.ports;

import software.ulpgc.chess.application.model.User;

public interface UserSerializer {
    String serialize(User user);
}
