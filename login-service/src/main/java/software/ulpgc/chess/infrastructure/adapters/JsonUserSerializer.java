package software.ulpgc.chess.infrastructure.adapters;

import software.ulpgc.chess.application.domain.model.User;
import software.ulpgc.chess.application.ports.UserSerializer;

public class JsonUserSerializer implements UserSerializer {
    @Override
    public String serialize(User user) {
        return "";
    }
}
