package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.ports.ErrorDialog;
import software.ulpgc.chess.application.ports.SessionHandler;
import software.ulpgc.chess.application.ports.LoginDialog;

public class LoginUseCase implements UseCase {
    private final LoginDialog input;
    private final SessionHandler sessionHandler;
    private final ErrorDialog output;

    public LoginUseCase(LoginDialog input, SessionHandler sessionHandler, ErrorDialog output) {
        this.input = input;
        this.sessionHandler = sessionHandler;
        this.output = output;
    }

    @Override
    public void execute() {
        if (sessionHandler.checkUser(input.username(), input.password())) {

        }
        else {
            output.show("El usuario no existe");
        }
    }
}
