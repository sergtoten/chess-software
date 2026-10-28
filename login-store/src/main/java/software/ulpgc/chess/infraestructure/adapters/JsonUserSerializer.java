package software.ulpgc.chess.infraestructure.adapters;

import software.ulpgc.chess.application.model.User;
import software.ulpgc.chess.application.ports.UserSerializer;

public class JsonUserSerializer implements UserSerializer {
    @Override
    public String serialize(User user) {
        return "";
    }
}
