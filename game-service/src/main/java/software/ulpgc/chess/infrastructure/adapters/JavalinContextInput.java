package software.ulpgc.chess.infrastructure.adapters;

import io.javalin.http.Context;
import software.ulpgc.chess.application.UseCase;

public class JavalinContextInput implements UseCase.Input {
    private final Context context;

    public JavalinContextInput(Context context) {
        this.context = context;
    }


    @Override
    public String get(String key) {
        String value = context.queryParam(key);
        return value != null ? value : context.formParam(key);
    }
}
