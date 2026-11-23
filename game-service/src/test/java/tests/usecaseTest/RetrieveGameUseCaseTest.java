package tests.usecaseTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.usecases.RetrieveGameUseCase;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RetrieveGameUseCaseTest {

    private static final String ID = "123e4567-e89b-42d3-a456-426614174000";
    private static final String STORED = "{\"id\":\"" + ID + "\"}";

    private final InMemoryGameStore store = new InMemoryGameStore();
    private final RetrieveGameUseCase useCase = new RetrieveGameUseCase(store);

    private UseCase.Output execute(Map<String, String> params) {
        return useCase.execute(params::get);
    }

    @Test
    @DisplayName("Sin id devuelve 400")
    void missingId() {
        assertThat(execute(Map.of()).status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Con un id mal formado (no UUID, path traversal o mayúsculas) devuelve 400")
    void malformedId() {
        store.save(ID, STORED);

        assertThat(execute(Map.of("id", "abc")).status()).isEqualTo(400);
        assertThat(execute(Map.of("id", "../x")).status()).isEqualTo(400);
        assertThat(execute(Map.of("id", ID.toUpperCase())).status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Con un id válido que no existe devuelve 404")
    void unknownId() {
        assertThat(execute(Map.of("id", ID)).status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Con un id existente devuelve 200 y el contenido guardado tal cual")
    void existingId() {
        store.save(ID, STORED);

        UseCase.Output output = execute(Map.of("id", ID));

        assertThat(output.status()).isEqualTo(200);
        assertThat(new String(output.result(), StandardCharsets.UTF_8)).isEqualTo(STORED);
    }
}
