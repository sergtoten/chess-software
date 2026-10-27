package software.ulpgc.chess.infraestructure.adapters;

import software.ulpgc.chess.application.ports.LoginDialog;

public class WebLoginDialog implements LoginDialog {
    @Override
    public String username() {
        return "";
    }

    @Override
    public String password() {
        return "";
    }
}
