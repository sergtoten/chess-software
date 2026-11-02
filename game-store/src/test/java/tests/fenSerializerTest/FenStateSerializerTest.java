package tests.fenSerializerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.application.domain.model.CastlingRight;
import software.ulpgc.chess.application.domain.model.Color;
import software.ulpgc.chess.application.domain.model.Square;
import software.ulpgc.chess.application.domain.model.State;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;
import software.ulpgc.chess.infrastructure.adapters.serializer.FenStateSerializer;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FenStateSerializerTest {

    private FenStateSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new FenStateSerializer();
    }

    @Test
    @DisplayName("Debe serializar el estado inicial")
    void shouldSerializeInitialState() {
        assertThat(serializer.serialize(State.initial()))
                .isEqualTo("w KQkq - 0 1");
    }

    @Test
    @DisplayName("Debe serializar el turno de las negras")
    void shouldSerializeBlackTurn() {
        State state = new State(Color.Black, Set.of(), Optional.empty(), 0, 1);

        assertThat(serializer.serialize(state)).startsWith("b ");
    }

    @Test
    @DisplayName("Debe escribir un guion cuando no hay derechos de enroque")
    void shouldSerializeNoCastlingRights() {
        State state = new State(Color.White, Set.of(), Optional.empty(), 0, 1);

        assertThat(serializer.serialize(state)).isEqualTo("w - - 0 1");
    }

    @Test
    @DisplayName("Debe serializar solo los derechos de enroque presentes")
    void shouldSerializeSomeCastlingRights() {
        State state = new State(
                Color.White,
                Set.of(CastlingRight.BlackLong, CastlingRight.WhiteShort),
                Optional.empty(), 0, 1);

        assertThat(serializer.serialize(state)).isEqualTo("w Kq - 0 1");
    }

    @Test
    @DisplayName("Debe escribir los enroques siempre en el orden KQkq")
    void shouldSerializeCastlingRightsInCanonicalOrder() {
        State state = new State(
                Color.White,
                Set.of(CastlingRight.BlackLong, CastlingRight.BlackShort,
                        CastlingRight.WhiteLong, CastlingRight.WhiteShort),
                Optional.empty(), 0, 1);

        assertThat(serializer.serialize(state)).isEqualTo("w KQkq - 0 1");
    }

    @Test
    @DisplayName("Debe serializar la casilla de captura al paso")
    void shouldSerializeEnPassantSquare() {
        State state = new State(Color.Black, Set.of(), Optional.of(Square.at("e3")), 0, 1);

        assertThat(serializer.serialize(state)).isEqualTo("b - e3 0 1");
    }

    @Test
    @DisplayName("Debe escribir un guion cuando no hay captura al paso")
    void shouldSerializeNoEnPassantSquare() {
        State state = new State(Color.White, Set.of(), Optional.empty(), 0, 1);

        assertThat(serializer.serialize(state)).isEqualTo("w - - 0 1");
    }

    @Test
    @DisplayName("Debe serializar los contadores de jugadas")
    void shouldSerializeMoveCounters() {
        State state = new State(Color.White, Set.of(), Optional.empty(), 17, 24);

        assertThat(serializer.serialize(state)).endsWith(" 17 24");
    }

    @Test
    @DisplayName("Debe separar los cinco campos con un solo espacio, sin espacios en los extremos")
    void shouldSeparateFieldsWithSingleSpaces() {
        State state = new State(
                Color.Black,
                Set.of(CastlingRight.WhiteShort),
                Optional.of(Square.at("d6")), 3, 9);

        String fields = serializer.serialize(state);

        assertThat(fields).isEqualTo("b K d6 3 9");
        assertThat(fields.split(" ")).hasSize(5);
        assertThat(fields).isEqualTo(fields.trim());
    }

    @Test
    @DisplayName("Debe recuperar los campos originales tras deserializar y volver a serializar")
    void shouldRoundTripWithDeserializer() {
        FenStateDeserializer deserializer = new FenStateDeserializer();

        String[] fens = {
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "8/8/8/8/8/8/8/8 b Kq e3 5 12",
                "4k3/8/8/8/8/8/8/4K3 w - - 17 24"
        };

        for (String fen : fens) {
            String expected = fen.substring(fen.indexOf(' ') + 1);
            assertThat(serializer.serialize(deserializer.deserialize(fen))).isEqualTo(expected);
        }
    }
}