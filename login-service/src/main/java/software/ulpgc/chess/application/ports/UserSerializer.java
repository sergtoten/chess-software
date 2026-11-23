package software.ulpgc.chess.application.ports;

import software.ulpgc.chess.application.domain.model.User;

public interface UserSerializer {
    String serialize(User user);
}
