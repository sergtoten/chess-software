package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;

import java.nio.charset.StandardCharsets;

record UseCaseOutput(int status, byte[] result) implements UseCase.Output {
    static UseCaseOutput error(int status, String message) {
        String body = "{\"error\":\"" + message + "\"}";
        return new UseCaseOutput(status, body.getBytes(StandardCharsets.UTF_8));
    }
}
