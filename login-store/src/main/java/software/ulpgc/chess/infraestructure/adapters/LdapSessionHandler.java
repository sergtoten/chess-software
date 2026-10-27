package software.ulpgc.chess.infraestructure.adapters;

import software.ulpgc.chess.application.ports.SessionHandler;

public class LdapSessionHandler implements SessionHandler {
    @Override
    public boolean checkUser(String usesrname, String password) {
        return false;
    }
}
